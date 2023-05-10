package com.example.myshoes.vendor.roomdb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProductViewModel(private val productDao: ProductDao) : ViewModel() {
      fun insertProduct(product: ProductEntity){
          viewModelScope.launch {
              productDao.insertProduct(product)
          }
      }
}