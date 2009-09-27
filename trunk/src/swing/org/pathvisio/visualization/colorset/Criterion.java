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
	public static final String[] TOKENS = {"AND", "OR", "=", "<", ">", "<=", ">=", "<>"};
	
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
	 * <p>
	 * Note that only String or Double values will work reliably.
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
		Object value = e.evaluate();
		if (value instanceof Boolean) return (Boolean)value;
		else 		
		{
			throw new CriterionException ("Expected Boolean expression");
		}
	}

	private Token parse() throws CriterionException {
		charNr = 0;
		input = expression;

		Token e = expression();
		Token t = getToken();
		if (t.type != TokenType.END)
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
				token = new Token(TokenType.NUMBER_LITERAL, Double.parseDouble(value));
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
				token = new Token(TokenType.LE);	
			else if (ch == '>')
			{
				token = new Token(TokenType.NE);
			}
			else
			{
				token = new Token(TokenType.LT);
				putBack (ch);
			}
			break;
		case '>':
			ch = eatChar();
			if (ch == '=')
				token = new Token(TokenType.GE);	
			else
			{
				token = new Token(TokenType.GT);
				putBack (ch);
			}
			break;
		case '=': 
			token = new Token(TokenType.EQ);
			break;
		case '(': 
			token = new Token(TokenType.LPAREN);
			break;
		case ')': 
			token = new Token(TokenType.RPAREN);
			break;
		case '[': {
			ch = eatChar();
			String value = "";
			while (ch != ']' && ch != '\0')
			{
				value += ch;
				ch = eatChar();
			}
			token = new Token(TokenType.ID, value);                 
		} break;
		case '"': {
			ch = eatChar();
			String value = "";
			while (ch != '"' && ch != '\0')
			{
				value += ch;
				ch = eatChar();
			}
			token = new Token(TokenType.STRING_LITERAL, value);                 
		} break;
		case 'A':	

			if (eatChar() == 'N' && eatChar() == 'D')
			{
				token = new Token (TokenType.AND);
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
				token = new Token (TokenType.OR);
			}
			else
			{
				throw new CriterionException("Invalid character 'O' at position " + (charNr - 1) + 
				"\n- Expected start of 'OR'");
			}
			break;
		case '\0':
			token = new Token (TokenType.END);
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
		if (t.type == TokenType.NUMBER_LITERAL)
		{
			getToken();
			result = t;
		}
		else if (t.type == TokenType.STRING_LITERAL)
		{
			getToken();
			result = t;
		}
		else if (t.type == TokenType.ID)
		{
			getToken();
			result = t;
		}
		else if (t.type == TokenType.LPAREN)
		{
			getToken();
			result = expression();
			t = getToken();			
			if (t.type != TokenType.RPAREN)
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
			morefactors -> "<=|=|>=|>|<" factor
						| empty
	 */
	private Token subterm() throws CriterionException
	{
		Token result;
		result = factor();
		Token t = getLookAhead();
		if (t.type == TokenType.EQ || t.type == TokenType.GE ||
				t.type == TokenType.LE || t.type == TokenType.GT ||
				t.type == TokenType.LT || t.type == TokenType.NE)
		{
			getToken();
			t.left = result;
			t.right = factor();
			result = t;
		}
		return result;
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
		Token t = getLookAhead();
		if (t.type == TokenType.AND)
		{
			getToken();
			t.left = result;
			t.right = term();
			result = t;
		}
		return result;
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
		Token t = getLookAhead();
		if (t.type == TokenType.OR)
		{
			getToken();
			t.left = result;			
			t.right = expression();
			result = t;
		}
		return result;
	}

	private enum TokenType {
		END,
		NUMBER_LITERAL,
		STRING_LITERAL,
		ID,
		EQ,
		GT,
		LT,
		GE,
		LE,
		NE,
		AND,
		OR,
		LPAREN,
		RPAREN;
	}
	/**
	 * returns true if arg is true, returns false if arg is false or null
	 */
	private boolean trueNotNull(Object arg) throws CriterionException
	{
		if (arg != null && !(arg instanceof Boolean)) throw new CriterionException
			("Expected type Boolean, got " + arg.getClass().getCanonicalName());
		return arg == null ? false : (Boolean)arg;
	}
	
	/**
	 * This class represents a single token of an expression
	 */
	private class Token {
		private TokenType type;
		private Object literalValue; // in case it is a number or string literal
		private String symbolValue; // in case it is a symbol or string literal

		private Token left = null;
		private Token right = null;

		void printMe (int level)
		{
			String result = "";
			for (int i = 0; i < level; ++i)
			{
				result += ("--- ");
			}
			result += type;
			switch (type)
			{
			case AND:
			case OR:
			case LE:
			case LT:
			case GT:
			case GE:
			case EQ:
			case NE:
				Logger.log.trace(result);
				left.printMe(level + 1);
				right.printMe(level + 1);
				break;
			case ID:
				Logger.log.trace(result + " [" + symbolValue + "]");
				break;
			case NUMBER_LITERAL:
			case STRING_LITERAL:
				Logger.log.trace(result + " (" + literalValue + ")");
				break;
			}
		}

		/** 
		 * Helper function. Check that both parameters are instances of Double,
		 * and both are non-null.
		 */
		private boolean isNonNullDouble(Object lval, Object rval)
		{
			return (lval != null && lval instanceof Double &&
					rval != null && rval instanceof Double);
		}
		
		/**
		 * May return null, meaning "NA"
		 */
		Object evaluate() throws CriterionException
		{
			Object lval = null;
			Object rval = null;
			if (left != null) lval = left.evaluate();
			if (right != null) rval = right.evaluate();
			String error = "";
			switch (type)
			{
			case AND:
				return Boolean.valueOf(
						trueNotNull(lval) && 
						trueNotNull(rval));
			case OR:
				return Boolean.valueOf(
						trueNotNull(lval) || 
						trueNotNull(rval));
			case EQ:
				return Boolean.valueOf (
						lval == null ? rval == null : lval.equals(rval));
			case NE:
				return Boolean.valueOf (
						!(lval == null ? rval == null : lval.equals(rval)));
			case GE:
				if (!isNonNullDouble (lval, rval)) return null;
				return Boolean.valueOf ((Double)lval >= (Double)rval);
			case LE:
				if (!isNonNullDouble (lval, rval)) return null;
				return Boolean.valueOf ((Double)lval <= (Double)rval);
			case GT:
				if (!isNonNullDouble (lval, rval)) return null;
				return Boolean.valueOf ((Double)lval > (Double)rval);
			case LT:
				if (!isNonNullDouble (lval, rval)) return null;
				return Boolean.valueOf ((Double)lval < (Double)rval);
			case ID:
				if(!symTab.containsKey(symbolValue)) {//symbol has no value
					error = "Sample '[" + symbolValue + "]' has no value";
					break;
				}
				return symTab.get(symbolValue);
			case NUMBER_LITERAL:
			case STRING_LITERAL:
				return literalValue;
			default:
				error = "Can't evaluate this expression";
			}
			throw new CriterionException(error);
		}

		Token (TokenType aType) { type = aType; literalValue = 0; symbolValue = ""; }
		Token (TokenType aType, String aValue)
		{ 
			type = aType; 
			if (aType == TokenType.ID)
			{
				literalValue = null; symbolValue = (String)aValue;
			}
			else
			{
				literalValue = aValue; symbolValue = "";
			}
		}
		Token (TokenType aType, double aValue) { type = aType; literalValue = aValue; symbolValue = ""; }
	}
}
