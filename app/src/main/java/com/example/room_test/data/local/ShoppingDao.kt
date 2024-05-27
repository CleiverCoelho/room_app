package com.example.room_test.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ShoppingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(shoppingItem: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(shoppingItem: ShoppingItem)

    @Query("SELECT * FROM shopping_items")
    fun observeAllShoppingItems(): LiveData<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE id= :id")
    fun observerShoppingItemById(id: Long): LiveData<ShoppingItem>

    @Query("SELECT SUM(price * amount) FROM shopping_items")
    fun observeTotalPrice(): LiveData<Float>
}