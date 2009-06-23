// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2009 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
// 
// http://www.apache.org/licenses/LICENSE-2.0 
//  
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License.
//
package org.pathvisio.plugins.directderby;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bridgedb.IDMapperException;
import org.bridgedb.rdb.DBConnector;
import org.pathvisio.data.DBConnDerby;

/**
 * Run this to open a simple command shell where
 * you can type SQL queries that are run against
 * a pgdb or pgex file.
 */
public class DirectDerby 
{
	private DBConnector con = new DBConnDerby();
	private Connection sqlcon = null;

	private boolean quit = false;
	
	DirectDerby (String database) throws IDMapperException
	{
		if (database.endsWith(".pgex"))
		{
			con.setDbType(DBConnector.TYPE_GEX);
		}
		else
		{
			con.setDbType(DBConnector.TYPE_GDB);
		}
		sqlcon = con.createConnection(database, 0); 
	}
	
	/**
	 * Check if the typed command is a complete SQL statement ending with ;
	 */
	private boolean isCompleteSql (String cmd)
	{
		return (cmd.trim().endsWith(";"));
	}
	
	/**
	 * Utility, repeat char c n times to form a string.
	 */
	private String repeat(char c, int n)
	{
		StringBuffer mystringbuf = new StringBuffer();
		 
		for ( int i=0; i< n; i++ )
		      mystringbuf.append ( c );
		 
		return mystringbuf.toString();
	}
	
	private static final int LIMIT = 100;
	/**
	 * Print the resultset as an ascii table.
	 * Prints at maximum 100 rows.
	 */
	private void printResultSet (ResultSet r) throws SQLException
	{
		int [] maxW;
		List<String[]> data = new ArrayList<String[]>();
		
		// dry run to find max column lengths
		
		int colnum = r.getMetaData().getColumnCount();
		maxW = new int[colnum];
		
		for (int i = 0; i < colnum; ++i)
		{
			maxW[i] = r.getMetaData().getColumnName(i+1).length();
		}
		
		int row = 0;
		while (r.next() && row < LIMIT)
		{
			String[] rowdata = new String[colnum];
			for (int i = 0; i < colnum; ++i) 
			{
				rowdata [i] = r.getString(i+1);
				if (rowdata[i] == null) rowdata[i] = "<null>";
				int len = rowdata[i].length();
				if (len > maxW[i]) maxW[i] = len;
			}
			row++;
			data.add(rowdata);
		}
		
		
		System.out.print ("+----+");
		for (int i = 0; i < colnum; ++i) System.out.print (repeat ('-', maxW[i]) + "+");
		System.out.println();
		
		System.out.print ("|    |");
		for (int i = 0; i < colnum; ++i) System.out.printf ("%" + maxW[i] + "s|", r.getMetaData().getColumnName(i + 1));
		System.out.println();
		
		System.out.print ("+----+");
		for (int i = 0; i < colnum; ++i) System.out.print (repeat ('-', maxW[i]) + "+");
		System.out.println();
		
		row = 0;
		for (String[] rowdata : data)
		{
			System.out.printf("|%4d|", new Object[] { row });
			for (int i = 0; i < colnum; ++i) System.out.printf ("%" + maxW[i] + "s|", rowdata[i]);
			row++;
			System.out.println();
		}

		System.out.print ("+----+");
		for (int i = 0; i < colnum; ++i) System.out.print (repeat ('-', maxW[i]) + "+");
		System.out.println();

	}
	
	/**
	 * Main loop, here commands are read from the keyboard and parsed.
	 */
	private void run()
	{
		BufferedReader in = new BufferedReader (new InputStreamReader (System.in));
		String cmd = "";
		while (!quit)
		{
			try
			{
				System.out.println ("Type an SQL statement or 'quit'");
				String line = in.readLine();
				if (line.trim().equalsIgnoreCase("quit"))
				{
					quit = true;
				}
				else if (line.trim().equalsIgnoreCase("show tables;"))
				{
					try
					{
						ResultSet r = sqlcon.getMetaData().getTables(null, "APP", null, null);
						printResultSet (r);
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					cmd += line;
					if (isCompleteSql (cmd))
					{
						try
						{
							cmd = cmd.substring(0, cmd.indexOf(';'));
							System.out.println ("Executing '" + cmd + "'");
							ResultSet r = sqlcon.createStatement().executeQuery(cmd);
							printResultSet (r);
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
						cmd = "";
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				quit = true;
			}
		}
	}
	
	public static void printUsage()
	{
		System.out.print (
				"DirectDerby\n" +
				"  command line shell for running sql queries on pgex / pgdb databases\n" +
				"\n" +
				"Usage:\n" +
				"  DirectDerby [database.pgdb|database.pgex]\n");
	}
	
	public static void main (String [] args) throws IDMapperException
	{
		if (args.length != 1)
		{
			printUsage();
			System.out.println ("Error: Expected one argument\n");
		}
		else if (!new File(args[0]).exists())
		{
			printUsage();
			System.out.println ("Error: file '" + args[0] + "' doesn't exist\n");
		}
		else
		{
			DirectDerby pvq = new DirectDerby(args[0]);
			pvq.run();
		}
	}
}
