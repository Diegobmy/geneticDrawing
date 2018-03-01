package com.ragabuza.drawningapp

import android.graphics.Bitmap
import android.graphics.Matrix
import processing.core.PApplet
import processing.core.PGraphics
import processing.core.PImage
import java.util.*


class Sketch<out T>(private val w: Int, private val h: Int, var bitmap: Bitmap, val callback: (image: Bitmap) -> T) : PApplet() {

    private val drawW: Float = w.toFloat()
    private val drawH: Float = h.toFloat()


    val maxPoly = 25

    private var canvas = createGraphics(w, h)
    private val imageCanvas = createGraphics(w, h)
    override fun settings() {
        size(w, h)
    }

    override fun setup() {
        callback.invoke(bitmap)
//        val img = loadImage("image.png")
        val img = PImage(bitmap)

        imageCanvas.beginDraw()
        imageCanvas.background(0F, 0F, 0F, 255F)
        imageCanvas.image(img, (drawW - bitmap.width) / 2, (drawH - bitmap.height) / 2)
        imageCanvas.loadPixels()
        imageCanvas.endDraw()

//        background(0F, 0F , 0F, 255F)
        canvas.beginDraw()
        canvas.background(0F, 0F, 0F, 255F)
        canvas.noStroke()

//        frameRate(1F)
        newNiceRect()
        niceRects.forEach { oldRects.add(it.copy()) }
    }

    private var niceRects = mutableListOf<niceRect>()
    private var oldRects = mutableListOf<niceRect>()


    fun MutableList<niceRect>.getFitness(): Float {
        canvas.background(0F, 0F, 0F, 255F)
        this.forEach {
            canvas.fill(
                    color(
                            it.red,
                            it.green,
                            it.blue,
                            alpha
                    )
            )
            canvas.quad(
                    it.x1, it.y1,
                    it.x2, it.y2,
                    it.x3, it.y3,
                    it.x4, it.y4
            )
        }
        canvas.loadPixels()
        var fitness = 0F
        for (index in 0 until canvas.pixels.size) {
            fitness += Math.abs(red(canvas.pixels[index]) - red(imageCanvas.pixels[index]))
            fitness += Math.abs(green(canvas.pixels[index]) - green(imageCanvas.pixels[index]))
            fitness += Math.abs(blue(canvas.pixels[index]) - blue(imageCanvas.pixels[index]))
        }
        return fitness
    }

    var attempt = 0

    fun nextGeneration() {
        val index = niceRects.size - 1
        if (niceRects[index].attempt > 0 && attempt != 500) {
            attempt++
            niceRects[index]
                    .mutate(random(0F, 255F), random(0F, 255F), random(0F, 255F), random(0F, drawW), random(0F, drawH))
        } else {
            attempt = 0
            niceRects[index].attempt = 0
            newNiceRect()
        }
    }

    var willPause = false

    override fun draw() {

        if (niceRects.size == maxPoly && niceRects.last().attempt == 0)
            willPause = true
        else
            nextGeneration()

        val oldFitness = oldRects.getFitness()
        val newFitness = niceRects.getFitness()

        niceRects.draw(newFitness)
        if (newFitness < oldFitness && attempt < 500) {
            oldRects.clear()
            niceRects.forEach {
                oldRects.add(it.copy())
            }
        } else {
            niceRects.clear()
            oldRects.forEach {
                niceRects.add(it.copy())
            }
        }

        background(0F, 0F, 0F, 255F)
        background(canvas)

        if (willPause) pause()
    }

    fun MutableList<niceRect>.draw(fitness: Float) {
        canvas.background(0F, 0F, 0F, 255F)
        this.forEach {
            canvas.fill(
                    color(
                            it.red,
                            it.green,
                            it.blue,
                            alpha
                    )
            )
            canvas.quad(
                    it.x1, it.y1,
                    it.x2, it.y2,
                    it.x3, it.y3,
                    it.x4, it.y4
            )
        }
        canvas.textSize(32F)
        canvas.fill(255)
        canvas.text("Similatidade:" + (fitness / 10000000) + "%", 0F, 32F)
        canvas.text(niceRects.size.toString() + "/$maxPoly", 0F, 64F)
    }

    private val alpha = 255F

    fun newNiceRect() {
        niceRects.add(niceRect(
                random(0F, 255F),
                random(0F, 255F),
                random(0F, 255F),
                random(0F, drawW),
                random(0F, drawH),
                random(0F, drawW),
                random(0F, drawH),
                random(0F, drawW),
                random(0F, drawH),
                random(0F, drawW),
                random(0F, drawH),
                20
        ))
    }

    data class niceRect(
            var red: Float,
            var green: Float,
            var blue: Float,
            var x1: Float,
            var y1: Float,
            var x2: Float,
            var y2: Float,
            var x3: Float,
            var y3: Float,
            var x4: Float,
            var y4: Float,
            var attempt: Int
    ) {
        fun mutate(rColor: Float, gColor: Float, bColor: Float, x: Float, y: Float) {
            attempt--
            when (Random().nextInt(10)) {
                1, 2, 3 -> {
                    red = rColor
                    green = gColor
                    blue = bColor
                }
                4 -> x1 = x
                5 -> y1 = y
                6 -> x2 = x
                7 -> y2 = y
                8 -> x3 = x
                9 -> y3 = y
                10 -> x4 = x
                11 -> y4 = y
            }
        }
    }


}