package com.example.lostfound.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfound.model.Complaint
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ComplaintViewModel : ViewModel() {
    private val _complaintList = MutableStateFlow<List<Complaint>>(emptyList())
    val complaintList: StateFlow<List<Complaint>> = _complaintList.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    private val firestore = FirebaseFirestore.getInstance()
    private val _posts = mutableStateListOf<Complaint>()

    init {
        fetchPosts()
    }
    fun fetchComplaints() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1000) // Simulate network delay
            _isRefreshing.value = false
        }
    }
    fun fetchPosts() {
        firestore.collection("complaints")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val postList = snapshot.toObjects(Complaint::class.java)
                    _complaintList.value = postList
                } else {
                    Log.d("Firestore", "No complaints found.")
                }
            }
    }
    fun getComplaintById(complaintId: String?): Flow<Complaint?> {
        return flow {
            if (complaintId != null) {
                val snapshot = firestore.collection("complaints")
                    .document(complaintId)
                    .get()
                    .await()

                if (snapshot.exists()) {
                    val complaint = snapshot.toObject(Complaint::class.java)
                    emit(complaint)
                } else {
                    emit(null)
                }
            } else {
                emit(null)
            }
        }.flowOn(Dispatchers.IO)
    }



}