package nl.tudelft.jpacman.level;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import nl.tudelft.jpacman.board.BasicSquare;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.Ghost;
import nl.tudelft.jpacman.points.DefaultPointCalculator;
import nl.tudelft.jpacman.sprite.PacManSprites;

/**
 * Test suite for the Level class.
 *
 * @author Test Student
 */
class LevelTest {

    private Level level;
    private Board board;
    private Square[][] squares;
    private List<Square> startSquares;
    private List<Ghost> ghosts;
    private CollisionMap collisions;
    private PacManSprites sprites;

    @BeforeEach
    void setUp() {
        // Create a simple 3x3 board
        squares = new Square[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                squares[x][y] = new BasicSquare();
            }
        }

        // Link squares together (create a grid)
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                Square north = (y > 0) ? squares[x][y - 1] : null;
                Square south = (y < 2) ? squares[x][y + 1] : null;
                Square west = (x > 0) ? squares[x - 1][y] : null;
                Square east = (x < 2) ? squares[x + 1][y] : null;

                if (north != null) squares[x][y].link(north, Direction.NORTH);
                if (south != null) squares[x][y].link(south, Direction.SOUTH);
                if (west != null) squares[x][y].link(west, Direction.WEST);
                if (east != null) squares[x][y].link(east, Direction.EAST);
            }
        }

        // Create board
        board = new Board(squares);

        // Create start squares
        startSquares = new ArrayList<>();
        startSquares.add(squares[0][0]);
        startSquares.add(squares[1][0]);

        // No ghosts for this simple test setup
        ghosts = new ArrayList<>();

        // Default collision map with a point calculator
        collisions = new DefaultPlayerInteractionMap(new DefaultPointCalculator());

        // Create level
        level = new Level(board, ghosts, startSquares, collisions);
    }

    //specification: a newly created level has a board.
    @Test
    void specificationLevelHasBoard() {
        assertThat(level.getBoard()).isNotNull();
        assertThat(level.getBoard()).isSameAs(board);
    }

    //specification: a newly created level is not in progress.
    @Test
    void specificationNewLevelNotInProgress() {
        assertThat(level.isInProgress()).isFalse();
    }

    //specification: starting a level sets it in progress.
    @Test
    void specificationStartingLevelSetsInProgress() {
        level.start();

        assertThat(level.isInProgress()).isTrue();
    }

    //specification: stopping a level clears in progress state.
    @Test
    void specificationStoppingLevelClearsInProgress() {
        level.start();
        level.stop();

        assertThat(level.isInProgress()).isFalse();
    }

    //specification: a player can be registered on a level.
    @Test
    void specificationRegisterPlayerAssignsSquare() {
        sprites = new PacManSprites();
        PlayerFactory playerFactory = new PlayerFactory(sprites);
        Player player = playerFactory.createPacMan();

        level.registerPlayer(player);

        assertThat(player.hasSquare()).isTrue();
        assertThat(player.getSquare()).isSameAs(squares[0][0]);
    }

    //specification: multiple players are assigned to different start squares.
    @Test
    void specificationRegisterMultiplePlayersAssignsDifferentSquares() {
        sprites = new PacManSprites();
        PlayerFactory playerFactory = new PlayerFactory(sprites);
        Player player1 = playerFactory.createPacMan();
        Player player2 = playerFactory.createPacMan();

        level.registerPlayer(player1);
        level.registerPlayer(player2);

        assertThat(player1.getSquare()).isSameAs(squares[0][0]);
        assertThat(player2.getSquare()).isSameAs(squares[1][0]);
    }

    //parameterized test: registering different numbers of players assigns them all to start squares.
    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void parameterizedRegisterPlayersAssignsToStartSquares(int playerCount) {
        sprites = new PacManSprites();
        PlayerFactory playerFactory = new PlayerFactory(sprites);
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < playerCount; i++) {
            Player player = playerFactory.createPacMan();
            level.registerPlayer(player);
            players.add(player);
        }

        for (Player player : players) {
            assertThat(player.hasSquare()).isTrue();
            assertThat(startSquares).contains(player.getSquare());
        }
    }

    //specification: registering the same player twice has no effect.
    @Test
    void specificationRegisteringPlayerTwiceHasNoEffect() {
        sprites = new PacManSprites();
        PlayerFactory playerFactory = new PlayerFactory(sprites);
        Player player = playerFactory.createPacMan();

        level.registerPlayer(player);
        Square firstSquare = player.getSquare();

        level.registerPlayer(player);

        assertThat(player.getSquare()).isSameAs(firstSquare);
    }

    //specification: an observer can be added to a level.
    @Test
    void specificationAddObserver() {
        TestLevelObserver observer = new TestLevelObserver();

        level.addObserver(observer);

        // Just verify no exception is thrown
        assertThat(observer).isNotNull();
    }

    //specification: an observer can be removed from a level.
    @Test
    void specificationRemoveObserver() {
        TestLevelObserver observer = new TestLevelObserver();

        level.addObserver(observer);
        level.removeObserver(observer);

        // Just verify no exception is thrown
        assertThat(observer).isNotNull();
    }

    //specification: a new level with no pellets has zero remaining pellets.
    @Test
    void specificationNewLevelZeroPellets() {
        assertThat(level.remainingPellets()).isEqualTo(0);
    }

    //specification: a level with at least one living player is considered alive.
    @Test
    void specificationLevelWithAlivePlayers() {
        sprites = new PacManSprites();
        PlayerFactory playerFactory = new PlayerFactory(sprites);
        Player player = playerFactory.createPacMan();

        level.registerPlayer(player);

        assertThat(level.isAnyPlayerAlive()).isTrue();
    }

    //private test utility class to capture observer notifications
    private static class TestLevelObserver implements Level.LevelObserver {
        private boolean levelWonCalled = false;
        private boolean levelLostCalled = false;

        @Override
        public void levelWon() {
            levelWonCalled = true;
        }

        @Override
        public void levelLost() {
            levelLostCalled = true;
        }
    }
    @Test
    void specificationStartingMultipleTimesKeepsInProgress() {
        level.start();
        level.start();

        assertThat(level.isInProgress()).isTrue();
    }
    
    //parameterized test: starting the level multiple times keeps it in progress.
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5})
    void parameterizedStartingKeepsLevelInProgress(int startCount) {
        for (int i = 0; i < startCount; i++) {
            level.start();
        }

        assertThat(level.isInProgress()).isTrue();
    }
    
    @Test
    void specificationStoppingMultipleTimesKeepsStopped() {
        level.start();
        level.stop();
        level.stop();

        assertThat(level.isInProgress()).isFalse();
    }
    
    //parameterized test: stopping the level multiple times keeps it stopped.
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5})
    void parameterizedStoppingKeepsLevelStopped(int stopCount) {
        level.start();
        
        for (int i = 0; i < stopCount; i++) {
            level.stop();
        }

        assertThat(level.isInProgress()).isFalse();
    }

    //specification: board remains the same after player registration.
    @Test
    void specificationBoardUnchangedAfterPlayerRegistration() {
        sprites = new PacManSprites();
        PlayerFactory playerFactory = new PlayerFactory(sprites);
        
        Board originalBoard = level.getBoard();
        Player player = playerFactory.createPacMan();
        level.registerPlayer(player);

        assertThat(level.getBoard()).isSameAs(originalBoard);
    }

    //jqwik property: starting multiple times is idempotent.
    @Property
    void propertyStartingIsIdempotent(@ForAll @IntRange(min = 1, max = 5) int startCount) {
        setUp();
        IntStream.range(0, startCount).forEach(i -> level.start());

        assertThat(level.isInProgress()).isTrue();
    }

    //specification: stopping after starting restores non-progress state.
    @Test
    void specificationStartStopTransitions() {
        assertThat(level.isInProgress()).isFalse();
        
        level.start();
        assertThat(level.isInProgress()).isTrue();
        
        level.stop();
        assertThat(level.isInProgress()).isFalse();
    }

    //jqwik property: multiple player registrations cycle through start squares.
    @Property
    void propertyPlayersOccupyStartSquares(@ForAll @IntRange(min = 1, max = 4) int playerCount) {
        setUp();
        sprites = new PacManSprites();
        PlayerFactory factory = new PlayerFactory(sprites);

        List<Player> players = new ArrayList<>();

        for (int i = 0; i < playerCount; i++) {
            Player p = factory.createPacMan();
            level.registerPlayer(p);
            players.add(p);
        }

        for (Player p : players) {
            assertThat(p.hasSquare()).isTrue();
        }
    }

    //specification: remaining pellets starts at zero in a new level.
    @Test
    void specificationRemainingPelletsNonNegative() {
        assertThat(level.remainingPellets()).isGreaterThanOrEqualTo(0);
    }

    //specification: multiple start-stop cycles preserve initial state.
    @Test
    void specificationStartStopCycleRestoresState() {
        boolean initialState = level.isInProgress();
        
        level.start();
        level.stop();
        level.start();
        level.stop();

        assertThat(level.isInProgress()).isEqualTo(initialState);
    }
}