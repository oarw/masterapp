package com.masterapp.data.repository

import com.masterapp.data.local.dao.TagDao
import com.masterapp.data.local.entity.Tag
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    fun getAllTags(): Flow<List<Tag>> =
        tagDao.getAllTags()
    
    fun searchTags(searchQuery: String): Flow<List<Tag>> =
        tagDao.searchTags("%$searchQuery%")
    
    suspend fun getTagById(tagId: Long): Tag? =
        tagDao.getTagById(tagId)
    
    suspend fun insertTag(tag: Tag): Long =
        tagDao.insert(tag)
    
    suspend fun updateTag(tag: Tag) =
        tagDao.update(tag)
    
    suspend fun deleteTag(tag: Tag) =
        tagDao.delete(tag)
    
    suspend fun deleteAllTags() =
        tagDao.deleteAllTags()
}