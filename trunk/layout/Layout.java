import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.applet.Applet;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.Timer;

class Layout {

/* This program optimises the 2D layout of a certain pathway (collection of blocks and linking lines), 
based on the coordinates of the blocks as stored in int[n][2] matrix "coord" 
(e.g. {{113,100},{124,232},{22,3},{301,122},{42,51}})
and the links between blocks as stored in int[m][2] matrix "link" (e.g. {{1,5},{5,4},{5,2},{2,3}})

NB int[][] link should only contain single links; so for example, if a certain row of link is {3,2}, then
there shouldn't be a row {2,3}

NB2 Block one is defined to have number zero
*/

//Declared methods: 

/** calculateSpringForce(int[][] coord, int[][] link, int refL, int sC)
Calculates the net force vector for every block caused by the forces exerted by links; links are
treated as being springs.
Returns double[][] springForce.
refL: reference length of the interconnecting springs
sC: spring constant of the interconnecting springs
Equation used: ((refL-distance)^2)/sC */

/** calculateElectricForce(int[][] coord, int[][] link, int alpha)
All blocks are treated as if they are particles having a charge of the same sign.
This method calculates the net force vector for every block caused by electrical repulsion forces;
The magnitude of the charge of a certain block is 1+nLinks; nLinks is the number of blocks this 
certain block is attached to. Blocks with a lot of connections thus have more space by bigger repulsion.
int alpha: repulsion constant
Equation used: (nLinks[first block]+1)*(nLinks[second block]+1)*(alpha/(distance squared) */

/** calculateDisplacement(double[][] forces, int displacementStepSize, int [] numFixedBlocks)
Displacements of the blocks are taken in the direction of the force vectors on the blocks. The magnitude
of a displacement is in the order of magnitude of displacementStepSize.
Blocks numFixedBlocks are kept on their initial position. */

/** coordinateFix(int[][] coord) has a int[][] coord as input and returns a int[][] in which all rows of
coord that were the same are now distinct.
This prevents blocks to have the same initial coordinates (by shifting overlapping blocks randomly 
for at most 10 pixels in both directions) */


//This program is based on the assumption every newly declared matrix is by default filled with zeros


/********************************************************************************************/

/* Method to calculate spring force vector between 2 blocks */

public static double[][] calculateSpringForce(int[][] coord, int[][] link, int refL, int sC) {
   //refL: reference length; sC: spring constant
	
	//Calculating coordinate difference between linked blocks	
	int[][] dCoord = new int[(link.length)][2];
	for(int nRow=0; nRow<(link.length); nRow++) {
		int[] blocksInvolved = {link[nRow][0],link[nRow][1]};
		int[] coordBlockOne = {coord[(blocksInvolved[0])-1][0],coord[(blocksInvolved[0])-1][1]};
		int[] coordBlockTwo = {coord[(blocksInvolved[1])-1][0],coord[(blocksInvolved[1])-1][1]};
		
		dCoord[nRow][0] = (coordBlockOne[0]-coordBlockTwo[0]);
		dCoord[nRow][1] = (coordBlockOne[1]-coordBlockTwo[1]);
		}
	
	//Calculating distance between linked blocks and using this to normalise dCoord as vectors of length one;
	//multiplying these later by the force magnitude gives the force vectors.	
	double[][] direction = new double[(link.length)][2];
	double[] distance = new double[(link.length)];
		
	for(int nRow=0; nRow<(link.length); nRow++) {
		int dx=dCoord[nRow][0];
		int dy=dCoord[nRow][1];
		distance[nRow] = Math.sqrt(dx*dx+dy*dy);
		direction[nRow][0]= ( dCoord[nRow][0] )/(distance[nRow]);
		direction[nRow][1]= ( dCoord[nRow][1] )/(distance[nRow]);
	}
				
	//Calculating spring force vector, using double[][] magnitudeSpringForce and double[][] direction
	double[] magnitudeSpringForce =  new double[(link.length)];
	for(int nRow=0; nRow<(link.length); nRow++) {
		magnitudeSpringForce[nRow]= (((refL-distance[nRow])*(refL-distance[nRow])) /sC );
	}
	
	// Calculating spring force vector
	int firstBlockInvolved=0;
	int secondBlockInvolved=0;
	double fx=0.0;
	double fy=0.0;
				
	double[][] springForce =  new double[(coord.length)][2];
	for(int nRow=0; nRow<(link.length); nRow++) {
		firstBlockInvolved=(link[nRow][0]-1);
		secondBlockInvolved=(link[nRow][1]-1);

		if (distance[nRow]<=refL) {
			//repulsion
			fx=magnitudeSpringForce[nRow]*(direction[nRow][0]);
			fy=magnitudeSpringForce[nRow]*(direction[nRow][1]);

			springForce[firstBlockInvolved][0]+=fx;
			springForce[firstBlockInvolved][1]+=fy;
			springForce[secondBlockInvolved][0]-=fx;
			springForce[secondBlockInvolved][1]-=fy;
			}
											
		else if (distance[nRow] > refL) {
			//attraction: opposite signs
			fx=magnitudeSpringForce[nRow]*(direction[nRow][0]);
			fy=magnitudeSpringForce[nRow]*(direction[nRow][1]);

			springForce[firstBlockInvolved][0]-=fx;
			springForce[firstBlockInvolved][1]-=fy;
			springForce[secondBlockInvolved][0]+=fx;
			springForce[secondBlockInvolved][1]+=fy;
			}
		}
	
	return springForce;
  }
  
/***********************************************************************************************/

/* Method to calculate electrical force between 2 blocks */

public static double[][] calculateElectricForce(int[][] coord, int[][] link, int alpha) {
	//alpha: electrical force constant
	
	//The number of links to a block is calculated; the charge of every block later is 
	//taken proportional to this number of links. This gives blocks with a lot of links 
	//more space (by bigger repulsion)
	int[] nLinks = new int[(coord.length)];
    
	for(int nRow=0; nRow<(link.length); nRow++) {
  		for(int nBlock=0; nBlock<=(coord.length); nBlock++) {	  
			if (link[nRow][0] == nBlock) {
	  			nLinks[nBlock-1]++;
				}
	  		else if (link[nRow][1] == nBlock) {
	  			nLinks[nBlock-1]++;
				}
			}
		}
	
	//Calculating coordinate difference vectors and storing them in array dCoord
	int[][][] dCoord = new int [(coord.length)][(coord.length)][2];
	 	
	for(int nRow=0; nRow<(coord.length); nRow++) {
		for(int nColumn=0; nColumn<(coord.length); nColumn++) {
			dCoord[nRow][nColumn][0]=(coord[nRow][0]-coord[nColumn][0]);
			dCoord[nRow][nColumn][1]=(coord[nRow][1]-coord[nColumn][1]);
			}
		}
			
	/*Calculating double[][] distanceSqeare and using this to normalise dCoord 
	as vectors of length one (stored in double[][] direction).	
	
	It is chosen to calculate the square of the distance and not the distance itself 
	to prevent unnescessary rounding since the square is needed in the force equation */ 
	
	double[][] distanceSquare = new double [(coord.length)][(coord.length)];
	
	for(int nRow=0; nRow<(coord.length); nRow++) {
		for(int nColumn=0; nColumn<(coord.length); nColumn++) {
			int dx=dCoord[nRow][nColumn][0];
			int dy=dCoord[nRow][nColumn][1];
			distanceSquare[nRow][nColumn]=((dx*dx)+(dy*dy));
			}
		}
	
	double[][][] direction = new double [(coord.length)][(coord.length)][2];
	double distance = 0;
	
	for(int nRow=0; nRow<(coord.length); nRow++) {
		for(int nColumn=0; nColumn<(coord.length); nColumn++) {
			distance=Math.sqrt(distanceSquare[nRow][nColumn]);
			if (distance > 0) {
				direction[nRow][nColumn][0]=((dCoord[nRow][nColumn][0])/distance);
				direction[nRow][nColumn][1]=((dCoord[nRow][nColumn][1])/distance);
			}
		}
	}

	//Calculating electric force magnitude
	double[][] electricForceMagnitude = new double [(coord.length)][(coord.length)];
	
	for(int nRow=0; nRow<(coord.length); nRow++) {
		for(int nColumn=0; nColumn<(coord.length); nColumn++) {
			if (distanceSquare[nRow][nColumn] != 0) {
				//Otherwise, the force magnitude keeps default value zero 
				electricForceMagnitude[nRow][nColumn]=1000*(nLinks[nRow]+1)*(nLinks[nColumn]+1)*(alpha/(distanceSquare[nRow][nColumn]));
				/* The force is weighed with the number of links of the corresponding blocks x1000; 1 is added to these values to prevent
				non-linked blocks of having zero force. */
				}
			}
		}
	
	//Calculating the net electric force on every block
	double[][]electricForce = new double [(coord.length)][2];
	
	for(int nRow=0; nRow<(coord.length); nRow++) {
		for(int nColumn=0; nColumn<(coord.length); nColumn++) {
		
			double fx=(direction[nRow][nColumn][0])*(electricForceMagnitude[nRow][nColumn]);
			double fy=(direction[nRow][nColumn][1])*(electricForceMagnitude[nRow][nColumn]);

			electricForce[nRow][0]+=fx;
			electricForce[nRow][1]+=fy;
			}
		}

	return electricForce;
	
	}


/***********************************************************************************************/

/* Method to calculate block displacements; blocks numFixedBlocks keep their initial position*/

public static int[][] calculateDisplacement(double[][] forces, int displacementStepSize, int[] numFixedBlocks ) {
	
	// First, the forces are normalised; the smallest component of the force vectors is normalised to one
	double[][] normalisedForces= new double [forces.length][2];
	
	//Finding scaling factor sF
	double sF = forces[0][0];
		
	for (int nRow=0; nRow<(forces.length); nRow++) {
		for (int nColumn=0; nColumn<2; nColumn++) {
			if((forces[nRow][nColumn])<sF) {
				sF=(forces[nRow][nColumn]);
				}
			}
		}		
	
	for (int nRow=0; nRow<(forces.length); nRow++) {
		for (int nColumn=0; nColumn<2; nColumn++) {
			normalisedForces[nRow][nColumn]= (forces[nRow][nColumn])/sF;
			}
		}
	
	//Displacements are taken proporional to and in the direction of normalisedForces. 
	//The multiplication factor is displacementStepSize
	int[][] displacement= new int [forces.length][2];

	for (int nRow=0; nRow<(forces.length); nRow++) {
		for (int nColumn=0; nColumn<2; nColumn++) {
			displacement[nRow][nColumn]=(int) -(displacementStepSize * normalisedForces[nRow][nColumn]);
			}
		}
	
	//Keeping blocks numFixedBlocks on their initial position
	for(int nRow=0; nRow<(numFixedBlocks.length); nRow++) {
		int numFixedBlock = numFixedBlocks[nRow];
		displacement[numFixedBlock][0]=0;
		displacement[numFixedBlock][1]=0;
		}	
	
	return displacement;
	
	}

	
/***********************************************************************************************/

/* Method to give blocks with the same initial coordinates random different coordinates */

	public static int[][] coordinateFix(int[][] coord) {
		
	int overlappingPointCheck = 1;	
	
	while(overlappingPointCheck != 0)	{	
		//Checking if blocks have same coordinate
		
		int[] overlappingPoint=new int [coord.length];

		for(int pointChecked=0; pointChecked<(coord.length); pointChecked++) {
			for(int nRow=0; nRow<(coord.length); nRow++) {
				if ((coord[pointChecked][0]==coord[nRow][0]) && (coord[pointChecked][1]==coord[nRow][1]) && (nRow!=pointChecked)) {
				//Statement to check if point has same coordinate as reference point without being the reference point itself
					overlappingPoint[nRow]=1;
					}
				}
			}

		overlappingPointCheck = 0;
		for(int nRow=0; nRow<(coord.length); nRow++) {
			overlappingPointCheck+=overlappingPoint[nRow];
			}
			
		//System.out.println("overlappingPointCheck: "+overlappingPointCheck);	
					
		//As long as blocks overlap: random coordinate reassignment
		if(overlappingPointCheck != 0) {
			for(int nRow=0; nRow<(coord.length); nRow++) {
				if (overlappingPoint[nRow]==1) {
					//Creating number between 0 and 10
					int random=(int)(10*(Math.random()));				
					coord[nRow][0]+=random;
					random=(int)(10*(Math.random()));
					coord[nRow][1]+=random;
					}
				}
			}
		}

		return coord;
	}

	
/***********************************************************************************************/

/* Method to project the forces on 8 discrete directions; it was hoped this would nicely
line out the pathway, but in the current form it didn't work */

public static double[][] allignForces(double[][] forces) {
	//Forces are casted to forces in the direction n*(pi/4); this allows for 8 force vector directions
	
	for(int nRow=0; nRow<(forces.length); nRow++) {
		double fx = forces[nRow][0];
		double fy = forces[nRow][1];
		double forceMagnitude = Math.sqrt((fx*fx)+(fy*fy));
		double angle = Math.tan((Math.abs(fy))/(Math.abs(fx)));
		int signX= ((int)fx)/((int)fx);
		int signY= ((int)fy)/((int)fy);
		
		if ((angle>0) && (angle <= ((Math.PI)/8))) {
			forces[nRow][0]=signX*forceMagnitude;
		 	forces[nRow][1]=0;
			}
			 
		if ((angle>((Math.PI)/8)) && (angle <= (3*(Math.PI)/8))) {
			forces[nRow][0]=signX*Math.sqrt(.5*forceMagnitude*forceMagnitude);
		 	forces[nRow][1]=signY*Math.sqrt(.5*forceMagnitude*forceMagnitude);
			}
			
			 
		if ((angle>(3*(Math.PI)/8)) && (angle <= ((Math.PI)/2))) {
			forces[nRow][0]=0;
		 	forces[nRow][1]=signY*forceMagnitude;
			}
		}
	
		return forces;
		
	}
	

/***********************************************************************************************/

/* Method to induced grid allignment by implementing a grid having charged nodes that attract blocks;
it was expected the blocks would be induced to put themselves on the nodes of the grid. Didn't give
the expected result */

public static double[][] calculateGridForces(int[][] coord, int refL, int alpha ) {
	int gridL = (int)(refL/8);
	//For a more precise grid, decrease gridL
	
	double[][] gridForces=new double [coord.length][2];
	
	for(int nRow=0; nRow<(coord.length); nRow++) {
		int numberLeftGridPoint=(int)((coord[nRow][0])/gridL);
		int numberRightGridPoint=numberLeftGridPoint+1;
		int numberUpperGridPoint=(int)((coord[nRow][1])/gridL);
		int numberLowerGridPoint=numberUpperGridPoint+1;
		
		int [][] gridCoord=
		{{numberLeftGridPoint*gridL, numberLowerGridPoint*gridL},
		{numberRightGridPoint*gridL, numberLowerGridPoint*gridL},
		{numberLeftGridPoint*gridL, numberUpperGridPoint*gridL},
		{numberRightGridPoint*gridL, numberUpperGridPoint*gridL},
		{coord[nRow][0],coord[nRow][1]}};
		//Coordinates of the 4 nearest grid points and the coordinates of the block itself.
		
		int [][] link = {{1,5},{2,5},{3,5},{4,5}};
		/* int [][] link is needed as an input argument for method calculateElectricForces
		By linking the block to the 4 surrounding grid points, method calculateElectricForces
		gives the block a 4 times bigger charge than the grid points. This way, the influence
		of the grid points is limited to prevent the grid of having to big an influence on
		layout optimalisation */
		
		double [][] forces = calculateElectricForce(coord, link, alpha);
		
		gridForces[nRow][0]=-forces[4][0];
		gridForces[nRow][1]=-forces[4][1];
		//The last row of double[][] forces contains the forces working of the block
		}
	
	return gridForces;
	
	}

	
/***********************************************************************************************/

/* Method to line out selected blocks vertically; this method is not yet completed, bug rich and
badly written (due to time pressure). The idea is that the user can, after the optimalisation algorithm
is run on the entire collection of blocks, select a number of blocks to be alligned vertically. These
blocks than all get the x-coordinate of the first block selected. Than, int[][] link is evaluated to
find all blocks directly connected to the alligned blocks. The optimalisation algorithm is than run again
on this subset of blocks, in which the alligned blocks keep their position (by storing their numbers in
int [] numberFixedBlocks, an input argument for the method calculateDisplacement).

	public static int[][] lineOutVertical(int[][] coord, int[][] link, int[] selectedBlocks) {
	 
	 int [] selectedBlocks contains the number (starting from zero) of the blocks to be alligned
	vertically. The first block in this list is the block that keeps its initial position. 
	 
	int xRef = coord[selectedBlocks[0]][0];
	for(int i=0; i<selectedBlocks.length; i++) {
		coord[selectedBlocks[i]][0]=xRef;
		}

	 Finding the blocks of which the positions need to be optimalised again;
	int [] relevantLinks contains a 1 for for every relevant row of int[][] link.
	
	int[] relevantLinks = new int[link.length];
	
	for(int nRow=0; nRow<link.length; nRow++) {
		for(int i=1; i<selectedBlocks.length; i++) {
		 starts for i=1 since the blocks attached to the block that keeps its initial
		position are not to be optimalised again 
			if (link[nRow][0]=selectedBlocks[i]) {
				relevantLink[nRow][0]=1;
				otherNumber=link[nRow][1];
				if (otherNumber!=selectedBlocks[0]) {
					for(int nRow=0; nRow<link.length; nRow++) {
						if (link[nRow][0]=otherNumber) {
							relevantLink[nRow][0]=1;
							}
						else if(link[nRow][1]=otherNumber) {
							relevantLink[nRow][1]=1;
							}
						}
					}
				}
				
			else if (link[nRow][1]=selectedBlocks[i]) {
				relevantLink[nRow][1]=1;
				otherNumber=link[nRow][0];
				if (otherNumber!=selectedBlocks[0]) {
					for(int nRow=0; nRow<link.length; nRow++) {
						if (link[nRow][0]=otherNumber) {
							relevantLink[nRow][0]=1;
							}
						else if(link[nRow][1]=otherNumber) {
							relevantLink[nRow][1]=1;
							}
						}
					}
				}

			//Making a list of all unique relevant blocks int[] relevantBlocks
			int numberChecked = 0;
			
			for (int nRow=0; nRow<link.length; nRow++) {
				for(int nColumn=0; nColumn<2; nColumn++) {
					if (relevantLinks[nRow][nColumn]==1) {
						numberChecked=link[nRow][nColumn];
						int refRow=nRow;
						int refColumn=nColumn;
						for (nRow=0; nRow<link.length; nRow++) {
							for(nColumn=0; nColumn<2; nColumn++) {
								if ((link[nRow][nColumn]==numberChecked)&&(nRow!=refRow)&&(nColumn!=refColumn)) {
									relevantLinks[nRow][nColumn]=0;
									}
								}
							}
						}
					}
				}

		//(...) [some code needs to be added here]
		
		//In the following, it is assumed that int[] blocksOptimalisedAgain is found, containing the numbers 
		//of the blocks of which the layout needs to be optimalised again.
				
		int[] newCoord = new int[blockOptimalisedAgain.length][2];
		for(int nRow=0; nRow<blockOptimalisedAgain.length; nRow++) {
			newCoord[nRow][0]=coord[blocksOptimalisedAgain[nRow]][0];
			newCoord[nRow][1]=coord[blocksOptimalisedAgain[nRow]][1];
			}
		
		int sC = 1;			
		int refL = 80;     
		int alpha = 2000000;	
		  
		//Calculating forces
		double[][] electricalForces = calculateElectricForce(newCoord, link, alpha);
		double[][] springForces = calculateSpringForce(newCoord, link, refL, sC);
		    
		//Combining both types of forces
		double[][] forces = new double [coord.length][2];
		
		for (int nRow=0; nRow<(forces.length); nRow++) {
			for (int nColumn=0; nColumn<2; nColumn++) {
				forces[nRow][nColumn]=(electricalForces[nRow][nColumn])+(springForces[nRow][nColumn]);
			}
		}
		
		//(...) [The code that needs to be added here is more or less the same as the layout
		//optimalisation as implemented in the following].
	*/


/***********************************************************************************************/

/*Program code: tryout */ 
	  
//initialising
  public static void main(String[] args) {
  
  TestPanel testPanel;
  
  //Pick one of the following example inputs.
    
  //int[][] coord={{1700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700}};
  //int[][] link={{1,3},{2,3},{3,8},{8,9},{9,10},{10,11},{11,14},{13,14},{12,13},{12,10},{14,21},{21,22},{22,23},{22,24},{22,25},{25,27},{27,28},{28,26},{25,26},{27,31},{29,31},{31,30},{21,32},{32,20},{32,19},{32,18},{16,18},{16,7},{6,7},{6,4},{4,3},{4,5},{16,15},{16,17}};
  
  //int [][]coord={{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700},{2700,2700}};
  //int [][]link={{1,2},{1,3},{1,4},{4,5},{4,6},{4,7}};
  
  int[][]coord={{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000}};
  int[][]link={{1,2},{1,3},{3,4},{3,5},{1,8},{8,7},{7,6},{2,9},{9,10},{9,11},{9,12},{12,13},{15,14},{15,19},{19,16},{12,15},{19,17},{19,18},{2,27},{27,28},{27,29},{29,30},{29,31},{2,20},{20,21},{20,26},{20,32},{32,25},{32,24},{32,23},{32,22}};
  
  //int[][]coord={{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000}};
  //int[][]link={{1,2},{2,3},{3,4},{4,5},{5,6},{6,1}};
  
  //int[][]coord={{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000}};
  //int[][]link={{1,2},{2,3},{3,4},{4,5},{5,6},{6,7},{7,8},{8,9},{9,1}};
  
  //int[][] coord={{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000},{3000,3000}};
  //int[][]link={{1,9},{2,5},{3,7},{1,2},{2,3},{1,3},{4,5},{5,6},{6,7},{7,8},{8,9},{9,4},{10,11},{11,12},{12,13},{13,14},{14,15},{15,16},{16,17},{17,18},{18,19},{19,20},{20,10},{6,13},{8,17},{19,9}};
  
  coord = coordinateFix(coord);
  // Preventing blocks to have the same initial coordinates (by shifting overlapping blocks randomly for at most 10 pixels in both directions)
    
	System.out.println("Initial coordinates are:");
	for (int nRow=0; nRow<(coord.length); nRow++) {
		System.out.println(coord[nRow][0]+"  "+coord[nRow][1]);
		}
	System.out.println();

  int sC = 1;			//To calculate spring forces, the following equation is used: ((refL-distance)^2)/sC
  int refL = 80;     
  int alpha = 2000000;	//To calculate electric forces. Equation used: (nLinks[nRow]+1)*(nLinks[nColumn]+1)*(alpha/(distanceSquare[nRow][nColumn])
  
  //Calculating forces
  double[][] electricalForces = calculateElectricForce(coord, link, alpha);
  double[][] springForces = calculateSpringForce(coord, link, refL, sC);
    
  //Combining both types of forces
  double[][] forces = new double [coord.length][2];
  int nRow=0;
  int nColumn=0;
  
  for (nRow=0; nRow<(forces.length); nRow++) {
		for (nColumn=0; nColumn<2; nColumn++) {
			forces[nRow][nColumn]=(electricalForces[nRow][nColumn])+(springForces[nRow][nColumn]);
			}
		}

	//Trying to get grid allignement by using method allignForces, which projects all forces on 8 discrete directions. Didn't work.	
	//	double[][] directedForces=allignForces(forces);
	//		for (nRow=0; nRow<(forces.length); nRow++) {
	//			for (nColumn=0; nColumn<2; nColumn++) {
	//				forces[nRow][nColumn]+=.01*(directedForces[nRow][nColumn]);
	//				}
	//			}	
	
	//Searching forceMaxInitial which can be used as a criterium to stop optimalisation
   double forceMaxInitial = forces[0][0];
	
	for (nRow=0; nRow<(forces.length); nRow++) {
		for (nColumn=0; nColumn<2; nColumn++) {
			if((forces[nRow][nColumn])>forceMaxInitial) {
				forceMaxInitial = forces[nRow][nColumn];
				}
			}
		}		
	
	System.out.println("forceMaxInitial is: "+forceMaxInitial);
	System.out.println();
	
	double forceMaxCycle = forceMaxInitial;
	
	int stepSize = 10;
	//Step size displacements
	
	//Starting optimalisation; counter i counts the number of optimalisation cycles
	int i=0;
	
	  JFrame f = new JFrame("Rene Test panel");
	  f.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {System.exit(0);}
	  });
	  JApplet renetest = new JApplet();
	  f.getContentPane().add(renetest, BorderLayout.CENTER);
	  f.pack();
	  f.setSize(new Dimension(1280,1024));
	  f.setVisible(true);
	  
	  renetest.getContentPane().setLayout(new BorderLayout());
	  testPanel = new TestPanel();
	  testPanel.setBackground(Color.white);
	  renetest.getContentPane().add(testPanel);
	  
  		testPanel.rectCoord = coord;
		testPanel.lines = link;
		testPanel.repaint();
   	
//	  Getting screen dimensions
//	  Dimension screenSize = testPanel.getsize();
//	  screenSize.getwidth();
//	  screenSize.getheight();
  
   
   /* Starting optimalisation cycle */
     
        	
	while(forceMaxCycle >(.000001*forceMaxInitial)) {
	//forces have to drop below 0,0001% of their initial value to stop optimalisation

		testPanel.rectCoord = coord;
		testPanel.lines = link;
		testPanel.paint(testPanel.getGraphics());
		
		//Calculating displacements
		int[]numberFixedBlocks={0};
		//keeping block 1 (which has number zero) in place; this prevents the whole thing from moving around the screen
		
		int[][] displacement = calculateDisplacement(forces, stepSize, numberFixedBlocks);
			
		//Updating coord
		for(nRow=0; nRow<(coord.length); nRow++) {
			for(nColumn=0; nColumn<(2); nColumn++) {
				coord[nRow][nColumn]+=displacement[nRow][nColumn];
				}
			}
		
		//System.out.println("Current coordinates are:");
		//for (int nRow=0; nRow<(coord.length); nRow++) {
		//	System.out.println(coord[nRow][0]+"  "+coord[nRow][1]);
		//	}
		//System.out.println();

		//Calculating new electrical, spring and grid forces
	   electricalForces = calculateElectricForce(coord, link, alpha);
	   springForces = calculateSpringForce(coord, link, refL, sC);
	   	   
    	//Updating forces
		for (nRow=0; nRow<(forces.length); nRow++) {
			for (nColumn=0; nColumn<2; nColumn++) {
				forces[nRow][nColumn]=(electricalForces[nRow][nColumn])+(springForces[nRow][nColumn]);

				}
			}
		
//		newForces=allignForces(forces);
//		
//		for (nRow=0; nRow<(forces.length); nRow++) {
//			for (nColumn=0; nColumn<2; nColumn++) {
//				forces[nRow][nColumn]+=.01*(newForces[nRow][nColumn]);
//				}
//			}		
		
		//Searching forceMaxCycle to determine if loop is to be executed again
  		forceMaxCycle = forces[0][0];
	
		for (nRow=0; nRow<(forces.length); nRow++) {
			for (nColumn=0; nColumn<2; nColumn++) {
				if((forces[nRow][nColumn])>forceMaxCycle) {
					forceMaxCycle=(forces[nRow][nColumn]);
					}
				}
			}	
		
		i=i+1;	

		}
		
		
	/* End of optimalisation cycle */
  	
	  
	System.out.println();
	System.out.println("forceMaxFinal is: "+forceMaxCycle+" (this is "+(100*(forceMaxCycle/forceMaxInitial))+"% of forceMaxInitial)");
	System.out.println();
  
  
	System.out.println("Final coordinates are:");
	for (nRow=0; nRow<(coord.length); nRow++) {
		System.out.println(coord[nRow][0]+"  "+coord[nRow][1]);
		}
	System.out.println();
	
	System.out.println("Number of optimalisation cycles: "+i);
	System.out.println();
		
	
//	double[] length = new double[link.length];
	
//	for(int m=0; m<link.length; m++) {
//	int dxpt = ((coord[link[m][1]-1][0]-coord[link[m][0]-1][0])*(coord[link[m][1]-1][0]-coord[link[m][0]-1][0]));
//	int dypt = ((coord[link[m][1]-1][1]-coord[link[m][0]-1][1])*(coord[link[m][1]-1][1]-coord[link[m][0]-1][1]));
//	length[m] = Math.sqrt(dxpt+dypt);
//	System.out.println("Length for link "+m+" is "+length[m]);
//	}
	  
  }
}	
