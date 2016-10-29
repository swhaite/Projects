import java.util.*;
import java.io.*;


class Sudoku
{
    /* SIZE is the size parameter of the Sudoku puzzle, and N is the square of the size.  For 
     * a standard Sudoku puzzle, SIZE is 3 and N is 9. */
    int SIZE, N;

    /* The grid contains all the numbers in the Sudoku puzzle.  Numbers which have
     * not yet been revealed are stored as 0. */
    int Grid[][];
    
    java.util.ArrayList<int[][]> stack = new java.util.ArrayList<int[][]>(); //stack arraylist to keep track of all the potential grids
    java.util.ArrayList<boolean[][][]> pStack = new java.util.ArrayList<boolean[][][]>(); //same but keeps track of the posibility grids 
    java.util.ArrayList<int[]> guessTracker = new java.util.ArrayList<int[]>(); //same but keeps track of guessing information: position of guesses on the grid and the number that was guessed.
    
    
    boolean pGrid[][][];//3d grid for all the posible locations numbers may go. pGrid[i][j][k] will return a boolean if a number k+1 may be placed at position i,j
    boolean solved = false; //used for checking if our work is done
    boolean failure = false; //used for checking if a guess went awry
    int index; //to keep track of the current possibility being tested
    
    public void print(int [][] array) //general print method (not linked to a sudoku object) for debugging.
    {
        // Compute the number of digits necessary to print out each number in the Sudoku puzzle
        int digits = (int) Math.floor(Math.log(N) / Math.log(10)) + 1;

        // Create a dashed line to separate the boxes 
        int lineLength = (digits + 1) * N + 2 * SIZE - 3;
        StringBuffer line = new StringBuffer();
        for( int lineInit = 0; lineInit < lineLength; lineInit++ )
            line.append('-');

        // Go through the Grid, printing out its values separated by spaces
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                printFixedWidth( String.valueOf( array[i][j] ), digits );
                // Print the vertical lines between boxes 
                if( (j < N-1) && ((j+1) % SIZE == 0) )
                    System.out.print( " |" );
                System.out.print( " " );
            }
            System.out.println();

            // Print the horizontal line between boxes
            if( (i < N-1) && ((i+1) % SIZE == 0) )
                System.out.println( line.toString() );
        }
    }
    
    public void replace(int ind) { //used once a solution has been found to change the original grid to the solved grid
    	for(int i = 0; i < N; i ++) {
    		Grid[i] = stack.get(ind)[i].clone();
    	}
    }

    public void createPossibilityGrid() { //creates the grid possibilities
    	pGrid = new boolean [N][N][N]; //pGrid[i][j][k]
    	for(int i = 0; i < N; i++) { //rows of pGrid
    		int p = i/SIZE; //current box x position
    		p = p*SIZE;
    		for(int j = 0; j < N; j++) { //columns of pGrid
    			int q = j/SIZE; //curent box y position
    			q = q*SIZE;
    			if(Grid[i][j] != 0) {
    				for (int k = 0; k < N; k++) {
    					pGrid[i][j][k] = false;
    				}
    				continue;
    			}
    			for(int k = 1; k <= N; k++) { //each individual number
    				pGrid[i][j][k-1] = true; //default possibility is true
    				//now I check if anything in that row, col or box would suggest otherwise
    				for(int l = 0; l < N; l++) { //row of Grid
    					if (Grid[i][l] == k) {pGrid[i][j][k-1] = false;}
    				}
    				for(int m = 0; m < N; m++) { //column of Grid
    					if (Grid[m][j] == k) {pGrid[i][j][k-1] = false;}
					}		
    				for(int n = 0; n < SIZE; n++) { //box of Grid
    					for(int o = 0; o < SIZE; o++) {
    						if (Grid[p+n][q+o] == k) {pGrid[i][j][k-1] = false;}
    					}
    					
				}
    			}
    		}
    	}
    }
    public boolean getPGrid(int i, int j, int k)  //getter method which deals with the possibility grid and stack
    {
    	return pStack.get(index)[i][j][k];
    }
    public int getGrid(int i, int j) {  //getter method which deals with the grid and stack
    	return stack.get(index)[i][j];
    }
    public void setPGrid(int i, int j, int k, boolean value) { //setter method which deals with the possibility grid and stack
    	pStack.get(index)[i][j][k] = value;
    }
    public void setGrid(int i, int j, int value) { //getter method which deals the the grid and stack. Also edits the possibility grid to reflect the change.
    	stack.get(index)[i][j] = value;
    	for (int a = 0; a < N; a++) { //row
    		setPGrid(a,j,value-1,false);
    	}
    	for (int a = 0; a < N; a++) { //column
    		setPGrid(i,a,value-1,false);
    		setPGrid(i,j,a,false);
    	}
    	int c = i/SIZE;
    	c = c*SIZE;
    	int d = j/SIZE;
    	d = d*SIZE;
    	for (int a = 0; a < SIZE; a++) { //box
    		for (int b = 0; b < SIZE; b++) {
    			setPGrid(c+a, d+b, value-1,false);
    		}
    	}
    }
    public int[][] copyGrid(int ind) { //creates a deep copy of a grid. Used when guessing.
    	int[][] newGrid = new int[N][N];
    	for(int i = 0; i < N; i++) {
    		newGrid[i] = stack.get(ind)[i].clone();
    	}
    	return newGrid;
    }
    
    public boolean[][][] copyPGrid(int ind) { //creates a deep copy of a possibility grid. Used when guessing. 
    	boolean[][][] newGrid = new boolean[N][N][N];
    	for(int i = 0; i < N; i++) {
    		for (int j = 0; j < N; j++) {
    			newGrid[i][j] = pStack.get(ind)[i][j].clone();
    		}
    	}
    	return newGrid;
    }
    
    //thanks to http://www.angusj.com/sudoku/hints.php for all the sudoku tips n tricks
    public boolean singles() { //go through every square. if only 1 option, take it
    	boolean Change = false;
    	for(int i = 0; i < N; i++) { //going through rows
    		for(int j = 0; j < N; j++) { //going through columns
    			if (getGrid(i,j) != 0) {continue;} //skip squares with numbers
    			int options = 0;
    			int position = 0;
    			for(int k = 0; k < N; k++) {
    				if (getPGrid(i,j,k)) {
    					options++;
    					position = k;
    				}
    			}
    			if (options == 1) { //if there was only one option for a specific spot on the grid, it must be taken.
    				Change = true;
    				setGrid(i,j,position+1);
    			}
    		}
    	}
    	return Change;
    }
    
    public boolean hiddenSingles() { //go through every row, column and box for every number. if a number has only 1 option in any of these it must be taken.
    	boolean Change = false;
    	for(int k = 0; k < N; k++) { //cycle through numbers
    		for(int i = 0; i < N; i++) { //cycle through rows
    			int options = 0;
    			int position = 0;
    			for(int j = 0; j < N; j++) { // cycle through columns in a row
    					if (getPGrid(i,j,k)) {
    						options++;
    						position = j;
    					}
    			}
    			if (options == 1) { //if a number has only one option in a row it must be taken
				Change = true;
    				setGrid(i,position,k+1);
    			}
    		}
    		for(int j = 0; j < N; j++) { // cycle through columns
    			int options = 0;
    			int position = 0;
    			for(int i = 0; i < N; i++) { // cycle through rows in that column
    					if (getPGrid(i,j,k)) {
    						options++;
    						position = i;
    					}
    			}
    			if (options == 1) { //if a number has only one option in a column it must be taken.
				Change = true;
    				setGrid(position,j,k+1);
    			}
			}
    		for(int n = 0; n < N; n+=SIZE) { //iterate through starting x positions of all the boxes
    			for(int m = 0; m < N; m +=SIZE) { //iterate through starting y positions of all the boxes
    				int options = 0;
    				int positionx = 0;
    				int positiony = 0;
    				for(int i = 0; i < SIZE; i++) { //iterate through the interior x position of a box
    					for (int j = 0; j < SIZE; j++) { //iterate through the interior y position of a box
    						if (getPGrid(i+n,j+m,k)) {
    							options++;
    							positionx = i+n;
    							positiony = j+m;
    						}
    					}
    				}
    				if (options == 1) { //if only one option for a number in a box it must be taken.
					Change = true;
    					setGrid(positionx, positiony, k+1);
    				}
    			}
    		}
    		
    	}
    	return Change;
    }
    
    public void guess() { 

    	int [] previousGuessInfo = guessTracker.get(index); //information from the last guess: {lastX, lastY, lastK}
    	if (failure) { //if the previous guess resulted in failure.
    		stack.remove(index); //remove the failed guess grids from the stack
    		pStack.remove(index);
    		index -=1;
    		for (int k = previousGuessInfo[2]+1; k < N; k++) { //try guessing with the next highest number
    			if (getPGrid(previousGuessInfo[0],previousGuessInfo[1],k)) {
    				previousGuessInfo[2] = k;
    				stack.add(copyGrid(index));
    				pStack.add(copyPGrid(index));
    				index +=1;
    				setGrid(previousGuessInfo[0],previousGuessInfo[1],k+1);
    				failure = false;
    				return; 				
    			}
    		}//if there is no next highest number, it was a guess sometime earlier that was the problem. Goes back another space on the stack.
    		guessTracker.remove(index+1);
    		guess();
    		return;
    	}
    	int a = previousGuessInfo[0]; //start guessing starting from the previous guess's x coordinate
    	int b = previousGuessInfo[1]+1; //start guessing starting from the spot after the previous guess's y coordinate.
    	
    	for(int i = a; i < N; i++) { //iterate through rows
    		for(int j = b; j < N; j++) { //iterate through columns
    			if (getGrid(i,j)==0) { //if there is an open spot:
    				for (int k = 0; k < N; k++) { //find the first available number and hope it works
    					if (getPGrid(i,j,k)) {
    						int [] newGuess = {i,j,k};
    						
    						stack.add(copyGrid(index));
    						pStack.add(copyPGrid(index));
    						guessTracker.add(newGuess);
    						index += 1;
    						setGrid(i,j,k+1);
    						return;
    					}
    				}//if a spot is not taken but has no possible options which may go there, something failed somewhere. Try another guess.
    				failure = true;
    				guess();
    				return;
    			}
    		}
    		b = 0;
    	}
    	
    }
    
    public void isSolved() {
    	//check if the current grid is full. if yes:solved!
    	//if not:
    	//	check every num. If number has no possibility in row/col/box and is not yet present, there was a failure somewhere. 
    	solved = true; 
    	//print(stack.get(index));
    	solving: //label used to break out of both forloops
    		for (int i = 0; i < N; i++) {
    			for (int j = 0; j < N; j++) {
    				if (getGrid(i,j) == 0) { //if there is an empty spot in the grid, the problem has not yet been solved.
    					solved = false;
    					break solving;
    				}
    			}
    		}
    	if (solved) {return;} //no use continuing, our work is done
    	for (int k = 0; k < N; k++) {//iterate through all the numbers
    		for (int i = 0; i < N; i++) { //rows
    			boolean option = false;
    			for (int j = 0; j < N; j++) {//cols in row
    				if (getGrid(i,j) == k+1) {
    					option = true;
    					break; //go to next row
    				}
    				if (getPGrid(i,j,k)) {
    					option = true;
    					break; //go to next row
    				}
    			}
    			if(!option) {//if there were no options, something failed
    				failure = true;
    				return;
    			}
    		}
    		for (int j = 0; j < N; j++) { //cols
    			boolean option = false;
    			for (int i = 0; i < N; i++) { //rows in col
    				if (getGrid(i,j) == k+1) {
    					option = true;
    					break; //go to next row
    				}
    				if (getPGrid(i,j,k)) {
    					option = true;
    					break; //go to next row
    				}
    			}
    			if(!option) {
    				//if there were no options, something failed
    				failure = true;
    				return;
    			}
    		}
    		for(int n = 0; n < N; n+=SIZE) { //iterate through box starting x position
    			for(int m = 0; m < N; m +=SIZE) { //iterate through box starting y position
    				boolean option = false;
    				boxLoop:
	    				for(int i = 0; i < SIZE; i++) { //iterate through x positions inside box
	    					for (int j = 0; j < SIZE; j++) { //iterate through y positions inside box
	    						if (getGrid(i+n,j+m) == k+1) {
	    							option = true;
	    							break boxLoop;
	    						}
	    						if (getPGrid(i+n,j+m,k)) {
	    							option = true;
	    							break boxLoop;
	    						}
	    					}
	    				}
    				if (!option) {
    					//if there were no options, something failed.
    					failure = true;
    					return;
    				}
    			}
    		}
    	}
    	
    }
    /* The solve() method should remove all the unknown characters ('x') in the Grid
     * and replace them with the numbers from 1-9 that satisfy the Sudoku puzzle. <--should be from 1-SIZE */
    public void solve()
    {
        
    	createPossibilityGrid(); //now i have the grid of all the possible placements for all the numbers
    	stack.add(Grid); //puts the Grid on the stack
    	pStack.add(pGrid); //puts the initial possibility grid on the stack
    	int [] noGuess = {0,0,0};
    	guessTracker.add(noGuess);//initial guess is not much of a guess at all, but it gives a starting point for the real guesses
    	index = 0;//starting position on stack: 0
    	while(!solved) { //while the puzzle is not yet solved:
    		if(singles()) { //check for singles. If something was changed, restart from beggining of loop to avoid guessing
    			continue;
    		}
    		if(hiddenSingles()) { //check for hidden singles. If something changed, restart from beggining nof loop to avoid guessing.
    			continue;
    		}
    		isSolved(); //checks to see if work is done AND if a guess resulted in failure
    		guess(); //guesses
    		
    	}
    	replace(index); //change Grid to match the final solution.
    }


    /*****************************************************************************/
    /* NOTE: YOU SHOULD NOT HAVE TO MODIFY ANY OF THE FUNCTIONS BELOW THIS LINE. */
    /*****************************************************************************/
 
    /* Default constructor.  This will initialize all positions to the default 0
     * value.  Use the read() function to load the Sudoku puzzle from a file or
     * the standard input. */
    public Sudoku( int size )
    {
        SIZE = size;
        N = size*size;

        Grid = new int[N][N];
        for( int i = 0; i < N; i++ ) 
            for( int j = 0; j < N; j++ ) 
                Grid[i][j] = 0;
    }


    /* readInteger is a helper function for the reading of the input file.  It reads
     * words until it finds one that represents an integer. For convenience, it will also
     * recognize the string "x" as equivalent to "0". */
    static int readInteger( InputStream in ) throws Exception
    {
        int result = 0;
        boolean success = false;

        while( !success ) {
            String word = readWord( in );

            try {
                result = Integer.parseInt( word );
                success = true;
            } catch( Exception e ) {
                // Convert 'x' words into 0's
                if( word.compareTo("x") == 0 ) {
                    result = 0;
                    success = true;
                }
                // Ignore all other words that are not integers
            }
        }

        return result;
    }


    /* readWord is a helper function that reads a word separated by white space. */
    static String readWord( InputStream in ) throws Exception
    {
        StringBuffer result = new StringBuffer();
        int currentChar = in.read();
	String whiteSpace = " \t\r\n";
        // Ignore any leading white space
        while( whiteSpace.indexOf(currentChar) > -1 ) {
            currentChar = in.read();
        }

        // Read all characters until you reach white space
        while( whiteSpace.indexOf(currentChar) == -1 ) {
            result.append( (char) currentChar );
            currentChar = in.read();
        }
        return result.toString();
    }


    /* This function reads a Sudoku puzzle from the input stream in.  The Sudoku
     * grid is filled in one row at at time, from left to right.  All non-valid
     * characters are ignored by this function and may be used in the Sudoku file
     * to increase its legibility. */
    public void read( InputStream in ) throws Exception
    {
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                Grid[i][j] = readInteger( in );
            }
        }
    }


    /* Helper function for the printing of Sudoku puzzle.  This function will print
     * out text, preceded by enough ' ' characters to make sure that the printint out
     * takes at least width characters.  */
    void printFixedWidth( String text, int width )
    {
        for( int i = 0; i < width - text.length(); i++ )
            System.out.print( " " );
        System.out.print( text );
    }


    /* The print() function outputs the Sudoku grid to the standard output, using
     * a bit of extra formatting to make the result clearly readable. */
    public void print()
    {
        // Compute the number of digits necessary to print out each number in the Sudoku puzzle
        int digits = (int) Math.floor(Math.log(N) / Math.log(10)) + 1;

        // Create a dashed line to separate the boxes 
        int lineLength = (digits + 1) * N + 2 * SIZE - 3;
        StringBuffer line = new StringBuffer();
        for( int lineInit = 0; lineInit < lineLength; lineInit++ )
            line.append('-');

        // Go through the Grid, printing out its values separated by spaces
        for( int i = 0; i < N; i++ ) {
            for( int j = 0; j < N; j++ ) {
                printFixedWidth( String.valueOf( Grid[i][j] ), digits );
                // Print the vertical lines between boxes 
                if( (j < N-1) && ((j+1) % SIZE == 0) )
                    System.out.print( " |" );
                System.out.print( " " );
            }
            System.out.println();

            // Print the horizontal line between boxes
            if( (i < N-1) && ((i+1) % SIZE == 0) )
                System.out.println( line.toString() );
        }
    }


    /* The main function reads in a Sudoku puzzle from the standard input, 
     * unless a file name is provided as a run-time argument, in which case the
     * Sudoku puzzle is loaded from that file.  It then solves the puzzle, and
     * outputs the completed puzzle to the standard output. */
    public static void main( String args[] ) throws Exception
    {
        InputStream in;
        if( args.length > 0 ) 
            in = new FileInputStream( args[0] );
        else
            in = System.in;

        // The first number in all Sudoku files must represent the size of the puzzle.  See
        // the example files for the file format.
        int puzzleSize = readInteger( in );
        if( puzzleSize > 100 || puzzleSize < 1 ) {
            System.out.println("Error: The Sudoku puzzle size must be between 1 and 100.");
            System.exit(-1);
        }

        Sudoku s = new Sudoku( puzzleSize );

        // read the rest of the Sudoku puzzle
        s.read( in );

        // Solve the puzzle.  We don't currently check to verify that the puzzle can be
        // successfully completed.  You may add that check if you want to, but it is not
        // necessary.
        s.solve();

        // Print out the (hopefully completed!) puzzle
        s.print();
    }
}