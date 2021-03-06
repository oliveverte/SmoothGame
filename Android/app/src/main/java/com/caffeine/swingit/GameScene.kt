package com.caffeine.swingit

import android.graphics.Color
import com.caffeine.swingit.Graphics.*
import com.caffeine.swingit.Graphics.GPoint
import com.caffeine.swingit.Graphics.GTools
import com.caffeine.swingit.GameScene.GameState
import android.icu.util.ULocale.getCountry
import android.R.raw
import android.media.MediaPlayer






class GameScene : GScene()
{
    enum class GameState{
        GAME_OVER,
        PLAY,
        WELCOME,
        NOT_INIT
    }


    enum class Weather {
        Rainy,
        Cloudy,
        Cleared,
        Stormy
    }

    companion object {
        const val ENNEMY_PROBABILITY = 0.005f
        val BASE_SIZE = GSize(70f, 60f)
        var weather = Weather.Cleared
        var ennemyProbability = ENNEMY_PROBABILITY
        var bonusID = R.drawable.litchi
        var bonusSize = BASE_SIZE
    }

    val SPEED: Float = 5f
    val BASE_TERRAIN_HEIGHT = 0.2f // Percentage of the screen that terrain take
    val UPPER_TERRAIN_HEIGHT = 0.25f // Percentage of base terrain that the upper terrain take
    val CHARACTER_SIZE = GSize(60f, 40f)
    val CHARACTER_XPOS = 0.2f // Percentage of screenWidth where character will be put on
    val CHARACTER_SPEED = 5f // Character displacement in pixel
    val CHARACTER_ROTATION = 25f // Rotation in degrees of the character when he moves
    val CHARACTER_LIFE = 100 // Max life that player can have
    val BONUS_VALUE = 18 // Value that bonus can get you if you take it
    val BONUS_PROBABILITY = 0.6f
    val CHARACTER_LIFE_DECREASE = 0.5f // character's life will be decreased each frame with this value
    val CLOUD_SIZE = GSize(140f, 80f) // Cloud size in the background
    val CLOUD_START_NUMBER = 4 // Number of cloud on the screen at start up
    var CLOUD_PROBABILITY = 0.5f
    val RAIN_SPEED = 17f
    val RAIN_SIZE = GSize(2f, 20f)
    val RAIN_SIZE_SMALL = GSize(1f, 13f)
    val THUNDERSTORM_DELAY = GInterval(1500f, 3000f)
    val MUSIC_GAMEOVER= MediaPlayer.create(GTools.activity, R.raw.gameover)
    val MUSIC_CATCH = MediaPlayer.create(GTools.activity, R.raw.taken)
    val MUSIC_BOMB = MediaPlayer.create(GTools.activity, R.raw.bomb)

    var timelapseItemGeneration = 1000L
    var timelapseCloudGenerator = 2000L
    private var gameState = GameState.NOT_INIT
    private var isAccelerometerEnable = true

    lateinit var terrain: Terrain
    private lateinit var bonusGenerator: BonusGenerator
    private lateinit var character: Character
    private lateinit var cloudGenerator: CloudGenerator
    private lateinit var rainGenerator: RainGenerator
    private lateinit var thunderstorm: Thunderstorm
    private lateinit var welcomeScreen: WelcomeScreen
    private lateinit var score_label: GLabel
    private lateinit var gameOverScreen: GameOverScreen
    private lateinit var sky: GSprite
    private lateinit var ennemiesGenerator: EnnemiesGenerator


    override fun didInitialized()
    {
        sky = GSprite(null, skyColor(), size, GPoint(size.width / 2, size.height / 2))
        println("size : $size")
        welcomeScreen = WelcomeScreen(this)
        terrain = Terrain(this)
        character = Character(this)
        bonusGenerator = BonusGenerator(this)
        cloudGenerator = CloudGenerator(this)
        rainGenerator = RainGenerator(this)
        thunderstorm = Thunderstorm(this)
        gameOverScreen = GameOverScreen(this)
        ennemiesGenerator = EnnemiesGenerator(this)
        score_label = GLabel("0")
        score_label.fontSize = 55
        score_label.color = Color.WHITE
        score_label.alpha = 125
        score_label.isHidden = false
        score_label.position = GTools.fromSceneToScreenPos(this.size, GPoint(0.5f, 0.8f))
        setFlagGameState(GameState.WELCOME)

        addChild(sky)
        if(weather == Weather.Stormy)
            addChild(thunderstorm)
        addChild(terrain)
        addChild(character)
        addChild(score_label)
    }


    fun skyColor(): Int
    {
        var color: Int = Color.rgb(122, 221, 255)
        if(weather == GameScene.Weather.Cleared) {
            color = Color.rgb(122, 221, 255)
            CLOUD_PROBABILITY = 0.005f
        }
        else if(weather == GameScene.Weather.Rainy) {
            color = Color.rgb(140, 157, 163)
            CLOUD_PROBABILITY = 0.3f
        }
        else if(weather == GameScene.Weather.Cloudy) {
            color = Color.rgb(174, 212, 226)
            CLOUD_PROBABILITY = 0.1f
        }
        else if(weather == GameScene.Weather.Stormy) {
            color = Color.rgb(99, 110, 119)
            CLOUD_PROBABILITY = 0.4f
        }
        return color
    }


    override fun start()
    {

    }


    override fun update(currentTime: Long)
    {

        cloudGenerator.update(currentTime)
        if(weather == Weather.Stormy || weather == Weather.Rainy)
            rainGenerator.update(currentTime)

        if(gameState == GameState.PLAY) {
            bonusGenerator.update(currentTime)
            ennemiesGenerator.update(currentTime)
        }

        for (child: GNode in children) {
            if (child !is IGUpdatable) continue
            child.update(currentTime)
        }
    }


    override fun touchSwipe(vectorIntermediate: GVector, startPos: GPoint, currentPos: GPoint)
    {
        super.touchSwipe(vectorIntermediate, startPos, currentPos)
        character.directionVector = GVector.normalize(vectorIntermediate)
    }


    override fun touchUp(pos: GPoint)
    {
        super.touchUp(pos)
        isAccelerometerEnable = true
        markAsAccelerometerReferencePosition()
        if(gameState == GameState.WELCOME) welcomeScreen.touchUp(pos)
        else if(gameState == GameState.GAME_OVER) gameOverScreen.touchUp(pos)
    }


    override fun touchDown(pos: GPoint) {
        super.touchDown(pos)
        isAccelerometerEnable = false
    }


    override fun onAccelerometerEvent(axisValues: FloatArray)
    {
        super.onAccelerometerEvent(axisValues)
        if(!isAccelerometerEnable || gameState != GameState.PLAY) return
        val directionVector = GVector(axisValues[1], axisValues[0])
        if(directionVector.dy != 0f)
            character.directionVector = GVector.normalize(directionVector)
    }


    fun increaseScore()
    {
        score_label.text = (score_label.text.toInt() + 1).toString()
    }


    fun setFlagGameState(_gameState: GameState)
    {

        if(gameState != GameState.NOT_INIT && gameState == _gameState) return
        gameState = _gameState
        if(_gameState == GameState.PLAY) {
            character?.reset()
            score_label.text = "0"
            markAsAccelerometerReferencePosition()
            gameOverScreen.hide()
            welcomeScreen.hide()
            character.enable = true
            score_label.isHidden = false
        } else if(_gameState == GameState.WELCOME) {
            character.reset()
            score_label.text = "0"
            character.enable = false
            gameOverScreen.hide()
            welcomeScreen.show()
            score_label.isHidden = true
        } else if(_gameState == GameState.GAME_OVER) {
            MUSIC_GAMEOVER.start()
            welcomeScreen.hide()
            gameOverScreen.show()
        }
    }


    fun getGameState(): GameState
    {
        return gameState
    }
}



















