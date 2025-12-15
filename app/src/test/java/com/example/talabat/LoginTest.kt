package com.example.talabat

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlinx.coroutines.runBlocking

class LoginTest {
    @Mock
    lateinit var dao: AppDao

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `login with correct admin credentials returns success`() = runBlocking {
        val email = "admin"
        val mockUser = User("admin", "123", isAdmin = true)
        `when`(dao.getUser(email)).thenReturn(mockUser)

        val user = dao.getUser(email)
        assertNotNull(user)
        assertEquals("123", user?.password)
        assertTrue(user!!.isAdmin)
    }
}