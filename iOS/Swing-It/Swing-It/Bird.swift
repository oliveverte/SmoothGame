//
//  Chacracter.swift
//  Swing-It
//
//  Created by Olivier Picard on 01/10/2018.
//  Copyright © 2018 Caffeine. All rights reserved.
//

import Foundation
import SpriteKit

class Bird : SKSpriteNode, IUpdatable, ICollisionableListener
{
    static let contactTestBitMask = 1
    var directionVector: CGVector!
    var isFalling = false
    var enable = true
    var _scene: GameScene!
    var lifeBar: ProgressBar!
    var _itemInCollisionWith: [SKNode] = []
    var itemInCollisionWith: [SKNode] {
        get { return _itemInCollisionWith }
        set { _itemInCollisionWith = newValue }
    }
    
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    
    init(scene: GameScene) {
        _scene = scene
        directionVector = CGVector.zero
        lifeBar = ProgressBar(maxValue: scene.CHARACTER_LIFE, size: CGSize(width: 50, height: 6))
        super.init(texture: SKTexture(imageNamed: "bird"),
                   color: UIColor.white,
                   size: scene.CHARACTER_SIZE)
        lifeBar.position = CGPoint(x: 0, y: -size.height/2 - lifeBar.size.height * 2)
        reset()
//        hide()
        addChild(lifeBar)
    }
    
    
    func hide() {
        self.alpha = 0
        lifeBar.alpha = 0
    }
    
    
    func reset() {
        directionVector = CGVector.zero
        self.position = CGPoint(x: _scene.size.width * _scene.CHARACTER_XPOS,
                                y: (_scene.size.height - _scene.terrain.TERRAIN_BOTTOM_LAYER_HEIGHT) / 2)
        lifeBar?.value = lifeBar.maxValue
        self.alpha = 1
        lifeBar.alpha = 1
        isFalling = false
    }
    
    
    func update(_ currentTime: TimeInterval) {
        if(alpha == 0) { return }
        if(!enable) { alpha = 0 ; lifeBar.alpha = 0 ; return }
        
        alpha = 1
        lifeBar.alpha = 1
        lifeBar.value -= _scene.CHARACTER_LIFE_DECREASE
        
        if(lifeBar.value <= 0) { isFalling = true }
        else if(position.y - size.height > _scene.size.height) { isFalling = true }
        else if(position.y - size.height < _scene.terrain.TERRAIN_POSITION) { isFalling = true }
        
        if(!isFalling) {
            let yDirection = directionVector.dy
            position.y += _scene.CHARACTER_SPEED * yDirection
            zRotation = (yDirection > 0)
                ? CGFloat(GLKMathDegreesToRadians(Float(_scene.CHARACTER_ROTATION) / 2))
                : CGFloat(GLKMathDegreesToRadians(Float(-_scene.CHARACTER_ROTATION) / 2))
            if(yDirection == 0) { zRotation = 0 }
        }
        else {
            if(_scene.getFlagGameState() == GameScene.GameState.PLAY) {
                _scene.setFlagGameState(_gameState: GameScene.GameState.GAME_OVER)
            }
            lifeBar.value = 0
            // Is Falling
            zRotation = CGFloat(GLKMathDegreesToRadians(-50))
            if(position.y <= _scene.terrain.TERRAIN_TOP_POS + size.height / 2) {
                position.y = _scene.terrain.TERRAIN_TOP_POS + size.height / 2
            }
            position.y -= _scene.CHARACTER_SPEED * 2
        }
    }
    
    
    func collisionEnter(node: SKNode) {
        if(alpha == 0) { return }
        let i = itemInCollisionWith.firstIndex(of: node)
        if(i != nil) { itemInCollisionWith.remove(at: i!) }
        _scene.removeChildren(in: [node])
        if(node is Ennemy) {
            isFalling = true
            run(_scene.SOUND_BOMB)
        } else if node is Bonus {
            lifeBar.value += _scene.BONUS_VALUE
            _scene.increaseScore()
            run(_scene.SOUND_TAKEN)
        }
    }
    
    
    func collisionExit(node: SKNode) { }
    func getFrame() -> CGRect { return frame }
    
    
}
