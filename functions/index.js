const {onSchedule} = require("firebase-functions/v2/scheduler");
const {initializeApp} = require("firebase-admin/app");
const {getFirestore, Timestamp} = require("firebase-admin/firestore");

initializeApp();

exports.deleteComplaints = onSchedule("every 1 minutes", async (event) => {
  const db = getFirestore();
  const now = Timestamp.now();
  const twoMinutesAgo = Timestamp.fromMillis(now.toMillis() - 2 * 60 * 1000);

  const snapshot = await db.collection("complaints")
      .where("_created_at", "<", twoMinutesAgo)
      .get();

  const deletePromises = [];
  snapshot.forEach((doc) => {
    deletePromises.push(db.collection("complaints").doc(doc.id).delete());
  });

  await Promise.all(deletePromises);
  console.log(`Deleted ${deletePromises.length} old complaints.`);
});
