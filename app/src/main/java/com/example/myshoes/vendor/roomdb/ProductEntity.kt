package com.example.myshoes.vendor.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity (
          @PrimaryKey val product_Id: String,
          val product_Name: String,
          val contact_Phone: String,
          val product_Image: String,
          val product_Price: String,
        )