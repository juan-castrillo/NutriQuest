package com.uneatlantico.encuestas.DB

import android.content.Context
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

class NutriQuestDB(context: Context) : SQLiteAssetHelper(context, DATABASE_NAME, "data/data/com.uneatlantico.encuestas/databases/", null, DATABASE_VERSION) {
    companion object {

        private val DATABASE_NAME = "EncuestasVacia.db"
        private val DATABASE_VERSION = 1
    }

}