package com.example.room_test.data.local

import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// for integrated test => MediumTest
// for ui components test => LargeTest

// tell the emulator these are instrumented tests
@RunWith(AndroidJUnit4::class)
// used for run unit tests
@SmallTest
class ShoppingDaoTest {
    private lateinit var database: ShoppingItemDatabase
    private lateinit var dao: ShoppingDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShoppingItemDatabase::class.java
        ).allowMainThreadQueries().build()
        // mainThreadQueries to guarantee that each test will happen one after another in the main thread
        dao = database.shoppingDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetItem() = runBlocking {
        val shoppingItem = ShoppingItem("banana", 2, 2f, "imageurl", 1)
        dao.insertShoppingItem(shoppingItem)

        // deal with assincronous LiveData type only for tests cases
        val liveDataShoppingItens : LiveData<List<ShoppingItem>> = dao.observeAllShoppingItems()
        val observedShoppingItens = liveDataShoppingItens.getOrAwaitValue()

        assertTrue(observedShoppingItens.contains(shoppingItem))
    }

    @Test
    fun deleteShoppingItem() = runBlocking {
        val shoppingItem = ShoppingItem("name", 1, 1f, "url", id = 1)
        dao.insertShoppingItem(shoppingItem)
        dao.deleteShoppingItem(shoppingItem)

        val liveDataAllShoppingItems = dao.observeAllShoppingItems()
        val observedShoppingItens = liveDataAllShoppingItems.getOrAwaitValue()

        assertFalse(observedShoppingItens.contains(shoppingItem))
    }

    @Test
    fun getShoppingItemById() = runBlocking {
        val shoppingItem = ShoppingItem("name", 1, 1f, "url", id = 1)
        dao.insertShoppingItem(shoppingItem)

        val liveDataShoppingItem = dao.observerShoppingItemById(1).getOrAwaitValue()

        assertTrue(liveDataShoppingItem == shoppingItem)
    }

    @Test
    fun observeTotalPriceSum() = runBlocking {
        val shoppingItem1 = ShoppingItem("name", 2, 10f, "url", id = 1)
        val shoppingItem2 = ShoppingItem("name", 4, 5.5f, "url", id = 2)
        val shoppingItem3 = ShoppingItem("name", 0, 100f, "url", id = 3)
        dao.insertShoppingItem(shoppingItem1)
        dao.insertShoppingItem(shoppingItem2)
        dao.insertShoppingItem(shoppingItem3)

        val totalPriceSum = dao.observeTotalPrice().getOrAwaitValue()

        assertTrue(totalPriceSum == (2 * 10f + 4 * 5.5f))
    }

}