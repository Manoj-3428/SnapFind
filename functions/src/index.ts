import * as functions from 'firebase-functions'
import * as admin from 'firebase-admin'
const { CloudTasksClient } = require('@google-cloud/tasks')
admin.initializeApp()

// Payload of JSON data to send to Cloud Tasks, will be received by the HTTP callback
interface ExpirationTaskPayload {
    docPath: string
}

// Description of document data that contains optional fields for expiration
interface ExpiringDocumentData extends admin.firestore.DocumentData {
    expiresIn?: number
    expiresAt?: admin.firestore.Timestamp
    expirationTask?: string
}

export const onCreateComplaint =
functions.firestore.document('/complaints/{docId}').onCreate(async snapshot => {
    const data = snapshot.data()! as ExpiringDocumentData
    const { expiresIn, expiresAt } = data

    let expirationAtSeconds: number | undefined
    if (expiresIn && expiresIn > 0) {
        expirationAtSeconds = Date.now() / 1000 + expiresIn
    }
    else if (expiresAt) {
        expirationAtSeconds = expiresAt.seconds
    }

    if (!expirationAtSeconds) {
        // No expiration set on this document, nothing to do
        return
    }

    // Get the project ID from the FIREBASE_CONFIG env var
    const project = JSON.parse(process.env.FIREBASE_CONFIG!).projectId
    const location = 'us-central1'
    const queue = 'complaints-ttl' // Changed queue name to complaints-ttl

    const tasksClient = new CloudTasksClient()
    const queuePath: string = tasksClient.queuePath(project, location, queue)

    const url = `https://${location}-${project}.cloudfunctions.net/firestoreTtlCallback`
    const docPath = snapshot.ref.path
    const payload: ExpirationTaskPayload = { docPath }

    const task = {
        httpRequest: {
            httpMethod: 'POST',
            url,
            body: Buffer.from(JSON.stringify(payload)).toString('base64'),
            headers: {
                'Content-Type': 'application/json',
            },
        },
        scheduleTime: {
            seconds: expirationAtSeconds
        }
    }

    const [response] = await tasksClient.createTask({ parent: queuePath, task })

    const expirationTask = response.name
    const update: ExpiringDocumentData = { expirationTask }
    await snapshot.ref.update(update)
})

export const firestoreTtlCallback = functions.https.onRequest(async (req, res) => {
    const payload = req.body as ExpirationTaskPayload
    try {
        await admin.firestore().doc(payload.docPath).delete()
        res.send(200)
    }
    catch (error) {
        console.error(error)
        res.status(500).send(error)
    }
})

export const onUpdateComplaintCancelExpirationTask =
functions.firestore.document('/complaints/{docId}').onUpdate(async change => {
    const before = change.before.data() as ExpiringDocumentData
    const after = change.after.data() as ExpiringDocumentData

    // Did the document lose its expiration?
    const expirationTask = after.expirationTask
    const removedExpiresAt = before.expiresAt && !after.expiresAt
    const removedExpiresIn = before.expiresIn && !after.expiresIn
    if (expirationTask && (removedExpiresAt || removedExpiresIn)) {
        const tasksClient = new CloudTasksClient()
        await tasksClient.deleteTask({ name: expirationTask })
        await change.after.ref.update({
            expirationTask: admin.firestore.FieldValue.delete()
        })
    }
})
