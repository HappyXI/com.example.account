package com.example.account.repository

import com.example.account.database.DatabaseConfig
import com.example.account.model.TableItem
import java.sql.ResultSet

object TableRepository {
    fun fetchTableItems(): List<TableItem> {
        val items = mutableListOf<TableItem>()
        val query = "SELECT user_no, ex_date, ex_ack, ex_category, ex_description FROM EXPENDITURE"

        val connection = DatabaseConfig.getConnection()
        val statement = connection.createStatement()
        val resultSet: ResultSet = statement.executeQuery(query)

        while (resultSet.next()) {
            val id = resultSet.getInt("user_no")
            val category = resultSet.getString("ex_category")
            val description = resultSet.getString("ex_description")
            val amount = resultSet.getInt("ex_ack")
            val date = resultSet.getString("ex_date")

            items.add(TableItem(id, category, description, amount, date))
        }

        resultSet.close()
        statement.close()
        connection.close()

        return items
    }
}