package com.example.account.database

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object DatabaseConfig {
    private const val DB_URL = "jdbc:postgresql://192.168.219.116:5432/postgres"
    private const val USER = "postgres"
    private const val PASSWORD = "1111"

    /*fun getConnection(): Connection {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD)
    }*/

    fun getConnection(): Connection {
        return try {
            println("Connecting to database...")
            DriverManager.getConnection(DB_URL, USER, PASSWORD).also {
                println("Connection successful!")
            }
        } catch (e: Exception) {
            println("Connection failed: ${e.message}")
            throw e
        }
    }
}