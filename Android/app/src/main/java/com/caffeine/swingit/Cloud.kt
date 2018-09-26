package com.caffeine.swingit

import android.graphics.Color
import com.caffeine.swingit.Graphics.*

class Cloud(val scene: GameScene, size: GSize, position: GPoint) :
        GSprite(null, Color.WHITE, size, position),
        IGUpdatable,
        IGDeletable
{
    init {
        alpha = 80
    }


    override fun update(currentTime: Long)
    {
        position.x -= scene.SPEED * 0.5f
    }


    override fun canBeDeleted(): Boolean { return (position.x + size.width / 2 + 5) < 0 }
}