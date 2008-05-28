// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2007 BiGCaT Bioinformatics
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

import org.pathvisio.data.GexManager;
import org.pathvisio.data.Sample;
import org.pathvisio.debug.Logger;

public class Criterion {
	static final String displaySample = "|Displayed sample|";
	public static final String[] tokens = {"AND", "OR", "=", "<", ">", "<=", ">="};
	private HashMap<String, Double> symTab;

	private String expression;
		
	private Exception parseException;
	
	public String getExpression() {  
		return expression == null ? "" : expression; 
	}
	
	public boolean setExpression(String expression) {
		this.expression = expression;
		return testExpression(expression);
	}
	
	public boolean setExpression(String expression, String[] symbols) {
		//Evaluate with dummy data:
		this.expression = expression;
		return testExpression(expression, symbols);
	}
	
	public boolean testExpression(String expression) {
		try {
			evaluate(expression);
			parseException = null;
			return true;
		} catch(Exception e) { 
			parseException = e;
			return false;
		}
	}
	
	public boolean testExpression(String expression, String[] symbols) {
		for(String s : symbols) {
			addSymbol(s, 1.0);
		}
		return testExpression(expression);
	}
	
	public Exception getParseException() { 
		return parseException;
	}

	void setSampleData(HashMap<Integer, Object> data) {
		// Add current sample values to symTab if they are of type Double
		HashMap<Integer, Sample> samples = GexManager.getCurrent().getCurrentGex().getSamples();
		clearSymbols();
		for(Sample s : samples.values()) {
			Object value = data.get(s.getId());
			if(value instanceof Double) addSymbol(s.getName(), (Double)value);
		}
	}
	
	public boolean evaluate(HashMap<Integer, Object> data, int displaySampleId) throws Exception {
		setSampleData(data);
		Object value = data.get(displaySampleId);
		if(value instanceof Double) addSymbol(displaySample, (Double)value);

		return evaluate(expression);
	}
	
	public boolean evaluate(HashMap<Integer, Object> data) throws Exception {
		setSampleData(data);
		return evaluate(expression);
	}
	
	public boolean evaluate(String[] symbols, double[] values) throws Exception {
		clearSymbols();
		for(int i = 0; i < symbols.length; i++) {
			symTab.put(symbols[i], values[i]);
		}
		return evaluate(expression);
	}

	public void addSymbol(String sym, Double val)
	{
		if(symTab == null) symTab = new HashMap<String, Double>();
		symTab.put(sym, val);
	}

	void clearSymbols()
	{
		if(symTab == null) return;
		symTab.clear();
	}
		
	//Boolean expression parser by Martijn
	String input;
	int charNr;
	boolean evaluate (String expr) throws Exception
	{
		Token e = parse(expr);
		return e.evaluateAsBool();
	}

	Token parse(String expr) throws Exception {
		charNr = 0;
		input = expr;

		Token e = expression();
		Token t = getToken();
		if (t.type != Token.TOKEN_END)
		{
			nextToken = null;
			throw new Exception("Multiple expressions found, second expression " +
					"starts at position " + charNr);
		}
		return e;
	}
	
	char eatChar()
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

	void putBack(char ch)
	{
		if (input.length() == 0 && ch == '\0')
		{
		}
		else
		{
			input = ch + input;
		}
	}

	Token nextToken = null;

	Token getLookAhead() throws Exception
	{
		nextToken = getToken();
		return nextToken;
	}

	// note: token is taken away from input!
	Token getToken() throws Exception
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
		case '.': {
			String value = "" + ch;
			ch = eatChar();
			while ((ch >= '0' && ch <= '9') || ch == '.')
			{
				value += ch;
				ch = eatChar();
			}
			putBack (ch);									
			token = new Token(Token.TOKEN_NUMBER, Double.parseDouble(value)); }                            
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
				throw new Exception("Invalid character 'A' at position " + (charNr - 2) + 
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
				throw new Exception("Invalid character 'O' at position " + (charNr - 1) + 
				"\n- Expected start of 'OR'");
			}
			break;
		case '\0':
			token = new Token (Token.TOKEN_END);
			break;
		default:
			throw new Exception("Unexpected end of expression at position " + charNr);
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
	Token factor() throws Exception
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
				throw new Exception("Number of opening and closing brackets does not match");
			}			
		}
		else
		{
			nextToken = null;
			throw new Exception("Wrong token at position " + charNr);
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
	Token subterm() throws Exception
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
	Token term() throws Exception
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
	Token expression() throws Exception
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
	class Token {
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

		boolean evaluateAsBool() throws Exception
		{
			switch (type)
			{
			case Token.TOKEN_AND:
				if (left.evaluateAsBool() && right.evaluateAsBool())
					return true;
				else
					return false;
			case Token.TOKEN_OR:
				if (left.evaluateAsBool() || right.evaluateAsBool())
					return true;
				else
					return false;
			case Token.TOKEN_EQ:
				if (left.evaluateAsDouble() == right.evaluateAsDouble())
					return true;
				else
					return false;
			case Token.TOKEN_GE:
				if (left.evaluateAsDouble() >= right.evaluateAsDouble())
					return true;
				else
					return false;
			case Token.TOKEN_LE:
				if (left.evaluateAsDouble() <= right.evaluateAsDouble())
					return true;
				else
					return false;
			case Token.TOKEN_GT:
				if (left.evaluateAsDouble() > right.evaluateAsDouble())
					return true;
				else
					return false;
			case Token.TOKEN_LT:
				if (left.evaluateAsDouble() < right.evaluateAsDouble())
					return true;
				else
					return false;
			}
			throw new Exception("Can't evaluate this expression as boolean");
		}

		double evaluateAsDouble() throws Exception
		{
			String error = "";
			switch (type)
			{
			case Token.TOKEN_ID:
				if(!symTab.containsKey(symbolValue)) {//symbol has no value
					error = "Sample '[" + symbolValue + "]' has no value";
					break;
				}
				return (Double)symTab.get(symbolValue);
			case Token.TOKEN_NUMBER:
				return numberValue;
			default:
				error = "Can't evaluate this expression as numeric";
			}
			throw new Exception(error);
		}

		Token (int _type) { type = _type; numberValue = 0; symbolValue = ""; }
		Token (int _type, double _numberValue) { type = _type; numberValue = _numberValue; symbolValue = ""; }
		Token (int _type, String _symbolValue) { type = _type; numberValue = 0; symbolValue = _symbolValue; }
	}
}


