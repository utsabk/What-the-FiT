package com.example.runningapp.ui.help

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.runningapp.R
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.android.synthetic.main.activity_help.*


class HelpActivity : AppCompatActivity() {

    private lateinit var fragment: ArFragment
    private var testRenderable: ViewRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        fragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment
        button_clear.setOnClickListener{ onClear() }


        if ((Build.VERSION.SDK_INT >= 24 &&
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.CAMERA
                    ) !=
                    PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                0
            )
        }

        val renderableFuture = ViewRenderable.builder()
            .setView(this, R.layout.ar_images)
            .build()
        renderableFuture.thenAccept {it -> testRenderable = it }

        fragment.setOnTapArPlaneListener {hitResult: HitResult?, plane: Plane?, motionEvent: MotionEvent? ->
            if (testRenderable == null) {
                return@setOnTapArPlaneListener
            }
            val anchor = hitResult!!.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(fragment.arSceneView.scene)
            val viewNode = TransformableNode(fragment.transformationSystem)
            viewNode.setParent(anchorNode)
            viewNode.renderable = testRenderable
            viewNode.select()
            viewNode.setOnTapListener{ hitTestRes: HitTestResult?, motionEv: MotionEvent? ->
                Toast.makeText(applicationContext, "Seriously??? You need help running this App??", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun onClear() {
        val children = ArrayList(fragment.arSceneView.scene.children)
        for (node in children) {
            if (node is AnchorNode) {
                if (node.anchor != null) {
                    node.anchor!!.detach()
                }
            }
        }
    }

}