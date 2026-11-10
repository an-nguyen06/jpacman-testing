package nl.tudelft.jpacman.board;

import nl.tudelft.jpacman.sprite.Sprite;

/**
 * Basic implementation of square.
 *
 * @author Jeroen Roosen 
 */
public class BasicSquare extends Square {

    /**
     * Creates a new basic square.
     */
    public BasicSquare() {
        super();
    }

    @Override
    public boolean isAccessibleTo(Unit unit) {
        return true;
    }

    @Override
    @SuppressWarnings("return.type.incompatible")
    public Sprite getSprite() {
        return null;
    }
}
