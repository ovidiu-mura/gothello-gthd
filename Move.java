/**
 * Move for Gamed, GameClient.
 *
 * @author Bart Massey
 * @version $Revision: 1.4 $
 */
public class Move {
    /**
     * Source square coordinates in the range 0-7, 0-7,
     * where the first coordinate is the column (x) value,
     * and the second is the row (y) value.
     */
    public int x1, y1;
    /**
     * Destination square coordinates in the range 0-7, 0-7,
     * where the first coordinate is the column (x) value,
     * and the second is the row (y) value.
     */
    public int x2, y2;

    /**
     * Create a move object from a description of the
     * form "coord-coord" where "coord" is an algebraic
     * square number.
     *
     * @param desc Move description.
     */
    public Move(String desc) {
	if (desc.length() != 5 || desc.charAt(2) != '-')
	    throw new IllegalArgumentException("bad move format");
	x1 = move_letter(desc.charAt(0));
	y1 = move_digit(desc.charAt(1));
	x2 = move_letter(desc.charAt(3));
	y2 = move_digit(desc.charAt(4));
    }

    /**
     * Create a move object from the given starting and
     * ending coordinates in the range 0-7.
     *
     * @param x1 Starting column.
     * @param y1 Starting row.
     * @param x1 Ending column.
     * @param y1 Ending row.
     */
    public Move(int x1, int y1, int x2, int y2) {
	this.x1 = x1;
	this.y1 = y1;
	this.x2 = x2;
	this.y2 = y2;
    }

    private static int move_letter(char ch) {
	switch (ch) {
	case 'a': return 0;
	case 'b': return 1;
	case 'c': return 2;
	case 'd': return 3;
	case 'e': return 4;
	case 'f': return 5;
	case 'g': return 6;
	case 'h': return 7;
	}
	throw new IllegalArgumentException("bad move letter");
    }

    private static int move_digit(char ch) {
	switch (ch) {
	case '1': return 0;
	case '2': return 1;
	case '3': return 2;
	case '4': return 3;
	case '5': return 4;
	case '6': return 5;
	case '7': return 6;
	case '8': return 7;
	}
	throw new IllegalArgumentException("bad move digit");
    }

    private static String square_name(int x, int y) {
	if (y < 0 || y >= 8)
	    throw new IllegalArgumentException("bad y coordinate in square");
	switch(x + 1) {
	case 1: return "a" + (y + 1);
	case 2: return "b" + (y + 1);
	case 3: return "c" + (y + 1);
	case 4: return "d" + (y + 1);
	case 5: return "e" + (y + 1);
	case 6: return "f" + (y + 1);
	case 7: return "g" + (y + 1);
	case 8: return "h" + (y + 1);
	}
	throw new IllegalArgumentException("bad x coordinate in square");
    }

    /**
     * Get a description of the move object.
     *
     * @return String of the form "coord-coord" where
     *         the coords are the starting and ending
     *         move coordinates.
     */
    public String name() {
	return square_name(x1, y1) + "-" + square_name(x2, y2);
    }
}
