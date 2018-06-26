package com.uneatlantico.encuestas.Inicio

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.CardView
import android.util.Log
import android.widget.*
import com.uneatlantico.encuestas.DB.NutriQuestExecuter.Companion.deleteAll
import com.uneatlantico.encuestas.DB.NutriQuestExecuter.Companion.ultimaPregunta
import com.uneatlantico.encuestas.R
import com.uneatlantico.encuestas.WSReceiver.EncuestaBuilder
import com.uneatlantico.encuestas.WSReceiver.firstConexion
import org.jetbrains.anko.doAsync

class NutriQuestMain : AppCompatActivity() {

    private lateinit var back: AppCompatImageButton //icono flecha atras
    private val fm = supportFragmentManager
    private val mHandler = Handler()
    private var doubleBackToExitPressedOnce = false //boolean para controlar doble click
    lateinit var nQController: NQController
    private lateinit var mensajeDespedida:TextView
    private lateinit var container:FrameLayout
    private lateinit var progress_bar: CardView
    private lateinit var bar: CardView
    private var questionNumber = 0
    private var numeroPregunta: Int = 0
    private var idEncuesta:Int = 1
    //private var fragmentTag:Int = 0
    private lateinit var reiniciar: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutri_quest_main)
        idEncuesta = intent.extras.getInt("idEncuesta")
        nQController = NQController(this.applicationContext, idEncuesta)
        mensajeDespedida = findViewById(R.id.mensajeDespedida)
        mensajeDespedida.alpha = 0.0F
        container = findViewById(R.id.container)
        progress_bar = findViewById(R.id.progress_bar)
        bar = findViewById(R.id.perma_bar)


        var idPreguntaInicio = 0
        try {
            idPreguntaInicio= nQController.ultimaPregunta()
        }catch (e:Exception){ Log.d("progresoExp", e.message) }

        if(idPreguntaInicio != 0){
            changeFragment(idPreguntaInicio)
        }
        else
            inicioEncuesta(idEncuesta)


        //TODO esconder boton de reiniciar si no tiene sentido
        //TODO calcular cual es la primera pregunta de la encuesta y volver ahi en lugar de 1
        reiniciar = findViewById(R.id.backEncuesta)
        //back.alpha = 0.0F
        reiniciar.setOnClickListener {
            deleteAll(this)
            inicioEncuesta(1)
        }

    }



    /**
     *
     */
    fun changeFragment(idPregunta: Int){
        val bundle = Bundle()

        //place holder de la clase NQcontroller
        val idActual = nQController.nextQuestion(idPregunta)
        numeroPregunta = nQController.numeroPregunta
        //abrir el fragmento con la siguiente pregunta
        if(idActual != -1){
            bundle.putInt("idPreguntaAnterior", idPregunta)
            bundle.putInt("idPreguntaActual", idActual)
            bundle.putInt("idEncuesta", idEncuesta)
            val tempfrag = QuestionFragment.newInstance()
            tempfrag.arguments = (bundle)
            openFragment(tempfrag)

            percentajeLeft(idActual)
        }

        //No quedan mas preguntas, se acabo la encuesta
        else {
            //mandarTodasLasRespuestas(this)
            removeFragment()
            container.alpha = 0.0F
            container.removeAllViews()
            mensajeDespedida.alpha = 1.0F
            percentajeLeft(questionNumber)
        }

    }

    /**
     * Aqui empieza la encuesta
     */
    fun inicioEncuesta(idPregunta: Int){

        val bundle = Bundle()
        bundle.putInt("idPreguntaAnterior", 0)
        bundle.putInt("idPreguntaActual", idPregunta)
        bundle.putInt("idEncuesta", idEncuesta)

        doAsync {
            nQController.inicioEncuesta(idPregunta)
            questionNumber = nQController.numeroPreguntas
            val tempfrag = QuestionFragment.newInstance()
            tempfrag.arguments = (bundle)
            resetFragment(tempfrag)
        }

    }

    private fun openFragment(fragment: Fragment) {
        fm.beginTransaction()
                .setCustomAnimations(R.anim.enter_right, R.anim.out_left, R.anim.enter_left,R.anim.out_right)
                .replace(R.id.container, fragment)

                .addToBackStack(null)
                .commit()
    }

    private fun resetFragment(fragment: Fragment) {
        fm.beginTransaction()
                .setCustomAnimations(0, 0, R.anim.enter_left,R.anim.out_right)
                .replace(R.id.container, fragment)

                .addToBackStack(null)
                .commit()

    }

    private fun removeFragment() {
        fm.beginTransaction()
                .setCustomAnimations(R.anim.enter_left, R.anim.out_right)
                .replace(R.id.container, Fragment())
                //.addToBackStack(null)
                .commit()
    }


    /**
     * Pulsar el boton de atras
     */
    override fun onBackPressed() {

        if(fm.backStackEntryCount <=1){
            if (doubleBackToExitPressedOnce) {
                System.exit(0)
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Presionar atrás de nuevo para salir", Toast.LENGTH_SHORT).show()
            mHandler.postDelayed(mRunnable, 1000)
        }

        else
            super.onBackPressed()


    }

    private val mRunnable = Runnable { doubleBackToExitPressedOnce = false }

    override fun onDestroy() {
        super.onDestroy()

        mHandler.removeCallbacks(mRunnable)
    }

    private fun mensaje(msg: String= "no especificado", ttl:String="titulo generico" ) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(msg).setTitle(ttl)
        val dialog = builder.create()
        dialog.show()
    }

    private fun percentajeLeft(idPregunta: Int){
        val anchMax = bar.width
        Log.d("max/actual", "$anchMax / $questionNumber")

        if(idPregunta == questionNumber || questionNumber == 0)
            progress_bar.layoutParams.width = bar.width
        else {
            val avance = anchMax / questionNumber
            progress_bar.layoutParams.width = numeroPregunta * avance
        }
    }
}
