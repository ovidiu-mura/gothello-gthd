import java.io.*;

public class Board {
    public static final int GAME_OVER = 1;
    public static final int CONTINUE = 0;
    public static final int ILLEGAL_MOVE = -1;
    public int game_state = CONTINUE;

    public int to_move = Connection.PLAYER_BLACK;
    public int serial = 1;

    // rules-specific game state 
    Board predecessor = null;
    private int square[][] = new int[8][8];
    private static final int WHITE_CHECKER = Connection.PLAYER_WHITE;
    private static final int BLACK_CHECKER = Connection.PLAYER_BLACK;

    public Board() {
	for (int i = 0; i < 8; i++)
	    for (int j = 0; j < 8; j++)
		square[i][j] = 0;
	for (int i = 1; i < 7; i++) {
	    square[i][0] = BLACK_CHECKER;
	    square[i][7] = BLACK_CHECKER;
	    square[0][i] = WHITE_CHECKER;
	    square[7][i] = WHITE_CHECKER;
	}
    }

    public Board(Board b) {
	predecessor = b.predecessor;
	for (int i = 0; i < 8; i++)
	    for (int j = 0; j < 8; j++)
		square[i][j] = b.square[i][j];

	to_move = b.to_move;
	game_state = b.game_state;
	serial = b.serial;
    }

    public void print(PrintStream s) {
	if (Gamed.time_controls)
	    s.print("381 ");
	else
	    s.print("380 ");
	s.print(serial);
	s.print(" ");
	if (Gamed.time_controls) {
	    s.print(Gamed.secs(Gamed.white_msecs));
	    s.print(" ");
	    s.print(Gamed.secs(Gamed.black_msecs));
	    s.print(" ");
	}
	if (game_state == GAME_OVER)
	    s.print("*");
	else if (to_move == Connection.PLAYER_WHITE)
	    s.print("n");
	else
	    s.print("s");
	s.print("\r\n");
	s.flush();
	s.print("382\r\n");
	for (int j = 7; j >= 0; --j)
	    for (int i = 0; i < 8; i++) {
		switch (square[i][j]) {
		case 0:  s.print("."); break;
		case BLACK_CHECKER:  s.print("b"); break;
		case WHITE_CHECKER:  s.print("w"); break;
		default: s.print("?");
		}
	    s.print("\r\n");
	}
	s.flush();
    }

    private static int opponent(int player) {
	if (player == Connection.PLAYER_WHITE)
	    return Connection.PLAYER_BLACK;
	if (player == Connection.PLAYER_BLACK)
	    return Connection.PLAYER_WHITE;
	throw new Error("internal error: bad player");
    }

    // XXX see declaration of BLACK_CHECKER, WHITE_CHECKER
    private static int checker_of(int player) {
	return player;
    }

    boolean same_position(Board b) {
	if (to_move != b.to_move)
	    return false;
	for (int i = 0; i < 8; i++)
	    for (int j = 0; j < 8; j++)
		if (square[i][j] != b.square[i][j])
		    return false;
	return true;
    }

    private boolean repeated_position() {
	Board p = predecessor;
	while(p != null) {
	    if (p.same_position(this))
		return true;
	    p = p.predecessor;
	}
	return false;
    }

    private static final int sgn(int x) {
	if (x > 0)
	    return 1;
	if (x < 0)
	    return -1;
	return 0;
    }

    private static final boolean clipped(int x) {
	if (x >= 8)
	    return true;
	if (x < 0)
	    return true;
	return false;
    }

    private int dist(int x, int y, int dx, int dy) {
	int d = 0;
	for(int q = -7; q <= 7; q++) {
	    int xx = x + q * dx;
	    int yy = y + q * dy;
	    if (clipped(xx) || clipped(yy))
		continue;
	    if (square[xx][yy] != 0)
		d++;
	}
	return d;
    }

    private boolean blocked(Move m, int dx, int dy, int d) {
	for (int q = 1; q < d; q++) {
	    int xx = m.x1 + q * dx;
	    int yy = m.y1 + q * dy;
	    if (square[xx][yy] == checker_of(opponent(to_move)))
		return true;
	}
	return false;
    }

    private boolean move_ok(Move m) {
	int dx = sgn(m.x2 - m.x1);
	int dy = sgn(m.y2 - m.y1);
	if (clipped(m.x1) || clipped(m.y1) ||
	    clipped(m.x2) || clipped(m.y2))
	    return false;
	int d = dist(m.x1, m.y1, dx, dy);
	if ((m.x2 - m.x1) * dx != d * dx)
	    return false;
	if ((m.y2 - m.y1) * dy != d * dy)
	    return false;
	if (blocked(m, dx, dy, d))
	    return false;
	if (square[m.x2][m.y2] == checker_of(to_move))
	    return false;
	return true;
    }

    private boolean has_moves() {
	for (int i = 0; i < 8; i++)
	    for (int j = 0; j < 8; j++)
		if (square[i][j] == checker_of(to_move)) {
		    for (int dx = -1; dx <= 1; dx++)
			for (int dy = -1; dy <= 1; dy++) {
			    if (dx == 0 && dy == 0)
				continue;
			    int d = dist(i, j, dx, dy);
			    Move m = new Move(i, j, i + d * dx, j + d * dy);
			    if (move_ok(m))
				return true;
			}
		}
	return false;
    }

    private int map_component(int x, int y, boolean map[][]) {
	int total = 1;
	map[x][y] = true;
	for (int dx = -1; dx <= 1; dx++)
	    for (int dy = -1; dy <=1; dy++) {
		if (dx == 0 && dy == 0)
		    continue;
		if (clipped(x + dx) || clipped(y + dy))
		    continue;
		if (map[x + dx][y + dy])
		    continue;
		total += map_component(x + dx, y + dy, map);
	    }
        return total;
    }

    private boolean connected(int side) {
	int i = 0;
	int j = 0;
	for (i = 0; i < 8; i++)
	    for (j = 0; j < 8; j++)
		if (square[i][j] == checker_of(side))
		    break;
	if (i >= 8 && j >= 8)
	    return true;
	boolean map[][] = new boolean[8][8];
	int ncomponents = map_component(i, j, map);
	for (i = 0; i < 8; i++)
	    for (j = 0; j < 8; j++)
		if (square[i][j] == checker_of(side) && !map[i][j])
		    return false;
	return true;
    }

    private static final boolean debug_try_move = false;

    public int try_move(Move m) {
	if (debug_try_move)
	    System.err.println("entering try_move()");
	if (game_state != CONTINUE) {
	    if (debug_try_move)
		System.err.println("leaving try_move(): move after game over");
	    return ILLEGAL_MOVE;
	}
	if (!has_moves()) {
	    game_state = GAME_OVER;
	    to_move = opponent(to_move);
	    if (debug_try_move)
		System.err.println("leaving try_move(): no legal moves");
	    return GAME_OVER;
	}
	if (!move_ok(m)) {
	    if (debug_try_move)
		System.err.println("leaving try_move(): illegal move");
	    return ILLEGAL_MOVE;
	}
	if (debug_try_move)
	    System.err.println("move ok");
	predecessor = new Board(this);

	// In this game, the moves are easy
	square[m.x2][m.y2] = square[m.x1][m.y1];
	square[m.x1][m.y1] = 0;

	if (connected(to_move)) {
	    game_state = GAME_OVER;
	    if (debug_try_move)
		System.err.println("leaving try_move(): move connected");
	    return GAME_OVER;
	}
	if (connected(opponent(to_move))) {
	    game_state = GAME_OVER;
	    to_move = opponent(to_move);
	    if (debug_try_move)
		System.err.println("leaving try_move(): move connected opponent");
	    return GAME_OVER;
	}
	if (repeated_position()) {
	    game_state = GAME_OVER;
	    to_move = Connection.OBSERVER;
	    if (debug_try_move)
		System.err.println("leaving try_move(): repeat draw");
	    return GAME_OVER;
	}

	to_move = opponent(to_move);
	if (to_move == Connection.PLAYER_BLACK)
	    serial++;
	if (debug_try_move)
	    System.err.println("leaving try_move(): continue game");
	return CONTINUE;
    }

    public int referee() {
	if (game_state != GAME_OVER)
	    throw new Error("internal error: referee unfinished game");
	return to_move;
    }

}
