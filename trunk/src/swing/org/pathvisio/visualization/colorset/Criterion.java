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
package org.pathvisio.visualization.colorset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pathvisio.debug.Logger;

/**
 * A criterion is a simple boolean expression that can 
 * be applied to a row in an expression dataset, and is used
 * for example in overrepresentation analysis to divide the data in two sets
 * (meets the criterion yes/no), or in color rules to decide whether a color 
 * applies or not.
 * 
 * This class handles tokenizing, parsing and evaluating 
 */
public class Criterion 
{
	/**
	 * Exception thrown e.g. when there is a syntax error in the criterion
	 */
	public static class CriterionException extends Throwable
	{
		CriterionException (String msg) { super (msg); }
		
	}
	
	private static final String DISPLAY_SAMPLE = "|Displayed sample|";
	
	/** operators that can be used in a Criterion */
	public static final String[] TOKENS = {"AND", "OR", "=", "<", ">", "<=", ">="};
	
	private Map<String, Object> symTab = new HashMap<String, Object>();

	private String expression = "";
		
	/**
	 * Get the current expression, an empty string by default.
	 */
	public String getExpression() 
	{  
		return expression; 
	}
	
	/**
	 * set and expression and available symbols.
	 * The symbols do not need to be mapped to values at this point.
	 * The expression will be parsed and checked for syntax errors
	 * 
	 * Returns an error String, or null if there was no error.
	 */
	public String setExpression(String expression, List<String> symbols) 
	{
		if (expression == null) throw new NullPointerException();
		this.expression = expression;

		for(String s : symbols) {
			symTab.put (s, 1.0);
		}
		try {
			evaluate();
			return null;
		} catch(CriterionException e) { 
			return e.getMessage();
		}
	}
	
	/** 
	 * Set symbol values.
	 * <p>
	 * You have to set all symbol values together. Any previously set
	 * symbol values are cleared.
	 */
	private void setSampleData(Map<String, Object> data)
	{
		symTab.clear();
		for(String key : data.keySet()) 
		{
			Object value = data.get(key);
			symTab.put (key, value);
		}
	}
	
	public boolean evaluate(Map<String, Object> data, String displaySampleId) throws CriterionException 
	{
		if (expression == null) throw new NullPointerException();
		setSampleData(data);
		Object value = data.get(displaySampleId);
		symTab.put (DISPLAY_SAMPLE, value);

		return evaluate();
	}
	
	public boolean evaluate(Map<String, Object> data) throws CriterionException {
		setSampleData(data);
		return evaluate();
	}
	
	//Boolean expression parser by Martijn
	String input;
	int charNr;
	private boolean evaluate () throws CriterionException
	{
		Token e = parse();
		return e.evaluateAsBool();
	}

	private Token parse() throws CriterionException {
		charNr = 0;
		input = expression;

		Token e = expression();
		Token t = getToken();
		if (t.type != Token.TOKEN_END)
		{
			nextToken = null;
			throw new CriterionException("Multiple expressions found, second expression " +
					"starts at position " + charNr);
		}
		return e;
	}
	
	private char eatChar()
	{
		if (input.length() == 0)
		{
			return '\0';
		}
		else
		{
			charNr++;
			char result = input.charAt(0);
			input = input.substring(1);
			return result;
		}
	}

	private void putBack(char ch)
	{
		if (input.length() == 0 && ch == '\0')
		{
		}
		else
		{
			input = ch + input;
		}
	}

	private Token nextToken = null;

	private Token getLookAhead() throws CriterionException
	{
		nextToken = getToken();
		return nextToken;
	}

	// note: token is taken away from input!
	private Token getToken() throws CriterionException
	{      
		Token token = null;
		if (nextToken != null)
		{
			token = nextToken;
			nextToken = null;
			return token;
		}

		// eat whitespace
		char ch = eatChar();

		while (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r')
		{
			ch = eatChar();
		}

		// read token
		switch (ch)
		{
		case '-':
		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
		case '.': 
		{
			String value = "" + ch;
			ch = eatChar();
			while ((ch >= '0' && ch <= '9') || ch == '.')
			{
				value += ch;
				ch = eatChar();
			}
			putBack (ch);									
			try
			{
				token = new Token(Token.TOKEN_NUMBER, Double.parseDouble(value));
			}
			catch (NumberFormatException e)
			{
				// most likely caused by typing a single "-"
				throw new CriterionException ("Invalid number '" + value + "'");
			}
		}                            
		break;
		case '<':
			ch = eatChar();
			if (ch == '=')
				token = new Token(Token.TOKEN_LE);	
			else
			{
				token = new Token(Token.TOKEN_LT);
				putBack (ch);
			}
			break;
		case '>':
			ch = eatChar();
			if (ch == '=')
				token = new Token(Token.TOKEN_GE);	
			else
			{
				token = new Token(Token.TOKEN_GT);
				putBack (ch);
			}
			break;
		case '=': 
			token = new Token(Token.TOKEN_EQ);
			break;
		case '(': 
			token = new Token(Token.TOKEN_LPAREN);
			break;
		case ')': 
			token = new Token(Token.TOKEN_RPAREN);
			break;
		case '[': {
			ch = eatChar();
			String value = "";
			while (ch != ']' && ch != '\0')
			{
				value += ch;
				ch = eatChar();
			}
			token = new Token(Token.TOKEN_ID, value);                 
		} break;
		case 'A':	

			if (eatChar() == 'N' && eatChar() == 'D')
			{
				token = new Token (Token.TOKEN_AND);
			}
			else
			{
				throw new CriterionException("Invalid character 'A' at position " + (charNr - 2) + 
				"\n- Expected start of 'AND'");
			}
			break;
		case 'O':
			ch = eatChar();
			if (ch == 'R')
			{
				token = new Token (Token.TOKEN_OR);
			}
			else
			{
				throw new CriterionException("Invalid character 'O' at position " + (charNr - 1) + 
				"\n- Expected start of 'OR'");
			}
			break;
		case '\0':
			token = new Token (Token.TOKEN_END);
			break;
		default:
			throw new CriterionException("Unexpected end of expression at position " + charNr);
		}
		//~ System.out.print (token.type + ", ");
		return token;
	}

	/*
		eats a factor
			forms:
			- number
			- identifier
			- "(" expression ")"
	 */
	private Token factor() throws CriterionException
	{
		Token result;
		Token t = getLookAhead();
		if (t.type == Token.TOKEN_NUMBER)
		{
			getToken();
			result = t;
		}
		else if (t.type == Token.TOKEN_ID)
		{
			getToken();
			result = t;
		}
		else if (t.type == Token.TOKEN_LPAREN)
		{
			getToken();
			result = expression();
			t = getToken();			
			if (t.type != Token.TOKEN_RPAREN)
			{
				nextToken = null;
				throw new CriterionException("Number of opening and closing brackets does not match");
			}			
		}
		else
		{
			nextToken = null;
			throw new CriterionException("Wrong token at position " + charNr);
		}
		return result;
	}


	/*
		eats a subterm
			forms:
			subterm -> factor morefactors
			morefactors -> "<=|=|>=|>|<" factor morefactors
						| empty
	 */
	private Token subterm() throws CriterionException
	{
		Token result;
		result = factor();
		while (true)
		{
			Token t = getLookAhead();
			if (t.type == Token.TOKEN_EQ || t.type == Token.TOKEN_GE ||
					t.type == Token.TOKEN_LE || t.type == Token.TOKEN_GT ||
					t.type == Token.TOKEN_LT)
			{
				getToken();
				t.left = result;
				t.right = subterm();
				result = t;
			}
			else
			{
				return result;
			}
		}		
	}

	/*
		eats a term
			forms:
			term -> subterm moresubterms
			moresubterms -> "AND" subterm moresubterms
						| empty
	 */
	private Token term() throws CriterionException
	{
		Token result;
		result = subterm();
		while (true)
		{
			Token t = getLookAhead();
			if (t.type == Token.TOKEN_AND)
			{
				getToken();
				t.left = result;
				t.right = term();
				result = t;
			}
			else
			{
				return result;
			}
		}
	}


	/* eats an expression
			forms:
			expression -> term moreterms
			moreterms -> "OR" term moreterms
				| empty
	 */
	private Token expression() throws CriterionException
	{
		Token result;
		result = term();
		while (true)
		{
			Token t = getLookAhead();
			if (t.type == Token.TOKEN_OR)
			{
				getToken();
				t.left = result;			
				t.right = expression();
				result = t;
			}
			else
			{
				return result;
			}				
		}
	}

	/**
	 * This class represents a single token of an expression
	 */
	private class Token {
		public int type;
		public static final int TOKEN_NONE = -2;
		public static final int TOKEN_END = -1;
		public static final int TOKEN_NUMBER = 0;
		public static final int TOKEN_ID = 1;
		public static final int TOKEN_EQ = 2;
		public static final int TOKEN_GT = 3;
		public static final int TOKEN_LT = 4;
		public static final int TOKEN_GE = 5;
		public static final int TOKEN_LE = 6;
		public static final int TOKEN_AND = 7;
		public static final int TOKEN_OR = 8;
		public static final int TOKEN_LPAREN = 9;
		public static final int TOKEN_RPAREN = 10;

		public double numberValue; // in case it is a number...
		public String symbolValue; // in case it is a symbol

		Token left = null;
		Token right = null;

		void printMe (int level)
		{
			for (int i = 0; i < level; ++i)
			{
				Logger.log.trace ("--- ");
			}
			switch (type)
			{
			case Token.TOKEN_AND:
				Logger.log.trace("AND");
				left.printMe(level + 1);
				right.printMe(level + 1);
				break;
			case Token.TOKEN_OR:
				Logger.log.trace("OR");
				left.printMe(level + 1);
				right.printMe(level + 1);
				break;
			case Token.TOKEN_LE:
				Logger.log.trace("<=");
				left.printMe(level + 1);
				right.printMe(level + 1);
				break;
			case Token.TOKEN_LT:
				Logger.log.trace("<");
				left.printMe(level + 1);
				right.printMe(level + 1);
				break;
			case Token.TOKEN_GT:
				Logger.log.trace(">");
				left.printMe(level + 1);
				right.printMe(level + 1);
				break;
			case Token.TOKEN_GE:
				Logger.log.trace(">=");
				left.printMe(level + 1);
				right.printMe(level + 1);
				break;
			case Token.TOKEN_EQ:
				Logger.log.trace("=");
				left.printMe(level + 1);
				right.printMe(level + 1);
				break;
			case Token.TOKEN_ID:
				Logger.log.trace("ID: " + symbolValue);
				break;
			case Token.TOKEN_NUMBER:
				Logger.log.trace("NUMBER: " + numberValue);
				break;
			}
		}

		/**
		 * returns true if arg is true, returns false if arg is false or null
		 */
		private boolean nullIsFalse(Boolean arg)
		{
			return arg == null ? false : arg;
		}
		
		Boolean evaluateAsBool() throws CriterionException
		{
			switch (type)
			{
			case Token.TOKEN_AND:
				if (nullIsFalse(left.evaluateAsBool()) && nullIsFalse(right.evaluateAsBool()))
					return true;
				else
					return false;
			case Token.TOKEN_OR:
				if (nullIsFalse(left.evaluateAsBool()) || nullIsFalse(right.evaluateAsBool()))
					return true;
				else
					return false;
			case Token.TOKEN_EQ:
				{
				Double lval = left.evaluateAsDouble();
				Double rval = right.evaluateAsDouble();
				if (lval == null || rval == null) return null;
				if (lval.equals(rval))
					return true;
				else
					return false;
				}
			case Token.TOKEN_GE:
				{
				Double lval = left.evaluateAsDouble();
				Double rval = right.evaluateAsDouble();
				if (lval == null || rval == null) return null;
				if (lval >= rval)
					return true;
				else
					return false;
				}
			case Token.TOKEN_LE:
				{
				Double lval = left.evaluateAsDouble();
				Double rval = right.evaluateAsDouble();
				if (lval == null || rval == null) return null;
				if (lval <= rval)
					return true;
				else
					return false;
				}
			case Token.TOKEN_GT:
				{
				Double lval = left.evaluateAsDouble();
				Double rval = right.evaluateAsDouble();
				if (lval == null || rval == null) return null;
				if (lval > rval)
					return true;
				else
					return false;
				}
			case Token.TOKEN_LT:
				{
				Double lval = left.evaluateAsDouble();
				Double rval = right.evaluateAsDouble();
				if (lval == null || rval == null) return null;
				if (lval < rval)
					return true;
				else
					return false;
				}
			}
			throw new CriterionException("Can't evaluate this expression as boolean");
		}

		/**
		 * May return null, meaning "NA"
		 */
		Double evaluateAsDouble() throws CriterionException
		{
			String error = "";
			switch (type)
			{
			case Token.TOKEN_ID:
				if(!symTab.containsKey(symbolValue)) {//symbol has no value
					error = "Sample '[" + symbolValue + "]' has no value";
					break;
				}
				Object value = symTab.get(symbolValue);
				if (value instanceof Double) return (Double)value;
				else return null;
			case Token.TOKEN_NUMBER:
				return numberValue;
			default:
				error = "Can't evaluate this expression as numeric";
			}
			throw new CriterionException(error);
		}

		Token (int aType) { type = aType; numberValue = 0; symbolValue = ""; }
		Token (int aType, double aNumberValue) { type = aType; numberValue = aNumberValue; symbolValue = ""; }
		Token (int aType, String aSymbolValue) { type = aType; numberValue = 0; symbolValue = aSymbolValue; }
	}
}


