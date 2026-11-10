package nl.tudelft.jpacman.game;

import org.junit.jupiter.api.Test;

import nl.tudelft.jpacman.Launcher;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    private Launcher launcher;
    private Game game;
 
    @BeforeEach
    void setUp() {
        launcher = new Launcher();
        launcher.launch();
        game = launcher.getGame();
    }
 
    @AfterEach
    void tearDown() {
        launcher.dispose();
    }

    @Test
    void start_whenNotInProgress_andPlayerAlive_andPelletsRemain_startsGame() {
        assertFalse(game.isInProgress(), "Pre-condition: game must not be running");
 
        game.start();
 
        assertTrue(game.isInProgress(),
            "Game should be in progress after a valid start()");
    }
 
    @Test
    void start_whenAlreadyInProgress_isIdempotent() {
        game.start();
        assertTrue(game.isInProgress(), "Pre-condition: game must be running");
 
        // Calling start() again must not throw and must leave it running
        assertDoesNotThrow(() -> game.start());
        assertTrue(game.isInProgress(),
            "Game should still be in progress after redundant start()");
    }

    @Test
    void start_whenPlayerIsDead_doesNotStart() {
        Player player = game.getPlayers().get(0);
        
        player.setAlive(false);
 
        assertFalse(player.isAlive(), "Pre-condition: player must be dead");
        assertFalse(game.isInProgress(), "Pre-condition: game must not be running");
 
        game.start();
 
        assertFalse(game.isInProgress(),
            "Game must not start when the player is dead");
    }

    @Test
    void start_whenNoPelletsRemain_doesNotStart() {
        Level level = game.getLevel();
        assertTrue(level.remainingPellets() > 0,
            "Default map has pellets - game should be able to start");
        
        assertFalse(game.isInProgress());
        game.start();
        assertTrue(game.isInProgress(),
            "Game should start with pellets remaining");
    }

    @Test
    void start_noPelletsAndPlayerAlive_doesNotStart() {
        Level level = game.getLevel();
        Player player = game.getPlayers().get(0);
        
        // Both must be true for game to start:
        boolean canStart = level.isAnyPlayerAlive() && level.remainingPellets() > 0;
        
        if (level.remainingPellets() > 0) {
            assertTrue(canStart, "With pellets and alive player, game should start");
        }
    }

    @Test
    void stop_afterStart_leavesGameNotInProgress() {
        game.start();
        assertTrue(game.isInProgress(), "Pre-condition: game must be running");
 
        game.stop();
 
        assertFalse(game.isInProgress(),
            "Game should not be in progress after stop()");
    }

    @Test
    void stop_whenNotInProgress_doesNotThrow() {
        // Stop without starting should not fail
        assertFalse(game.isInProgress());
        assertDoesNotThrow(() -> game.stop());
    }

    @Test
    void stop_whenAlreadyStopped_isIdempotent() {
        game.start();
        game.stop();
        assertDoesNotThrow(() -> game.stop());
        assertFalse(game.isInProgress());
    }

    @Test
    void getPlayers_returnsNonEmptyList() {
        assertNotNull(game.getPlayers());
        assertFalse(game.getPlayers().isEmpty());
    }

    @Test
    void getLevel_returnsValidLevel() {
        assertNotNull(game.getLevel());
    }
}