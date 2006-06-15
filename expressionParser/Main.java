// TODO

/*

* +token class
* +print parse tree
* +store parse tree in memory
* store tokens in a vector
* give "OR" lowest precedence
* improve testing: also test for syntax errors
* move tokenizer to a separate class
* more useful error messages

*/

import java.util.HashMap;
import java.util.Iterator;

public class Main {

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
				System.out.print ("--- ");
			}
			switch (type)
			{
				case Token.TOKEN_AND:
					System.out.println("AND");
					left.printMe(level + 1);
					right.printMe(level + 1);
				break;
				case Token.TOKEN_OR:
					System.out.println("OR");
					left.printMe(level + 1);
					right.printMe(level + 1);
				break;
				case Token.TOKEN_LE:
					System.out.println("<=");
					left.printMe(level + 1);
					right.printMe(level + 1);
				break;
				case Token.TOKEN_LT:
					System.out.println("<");
					left.printMe(level + 1);
					right.printMe(level + 1);
				break;
				case Token.TOKEN_GT:
					System.out.println(">");
					left.printMe(level + 1);
					right.printMe(level + 1);
				break;
				case Token.TOKEN_GE:
					System.out.println(">=");
					left.printMe(level + 1);
					right.printMe(level + 1);
				break;
				case Token.TOKEN_EQ:
					System.out.println("=");
					left.printMe(level + 1);
					right.printMe(level + 1);
				break;
				case Token.TOKEN_ID:
					System.out.println("ID: " + symbolValue);
				break;
				case Token.TOKEN_NUMBER:
					System.out.println("NUMBER: " + numberValue);
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
			throw new Exception();
		}
		
		double evaluateAsDouble() throws Exception
		{
			switch (type)
			{
				case Token.TOKEN_ID:
					return (Double)symTab.get(symbolValue);
				case Token.TOKEN_NUMBER:
					return numberValue;
			}
			throw new Exception();
		}
		
		Token (int _type) { type = _type; numberValue = 0; symbolValue = ""; }
		Token (int _type, double _numberValue) { type = _type; numberValue = _numberValue; symbolValue = ""; }
		Token (int _type, String _symbolValue) { type = _type; numberValue = 0; symbolValue = _symbolValue; }
	}
		
	HashMap symTab = new HashMap();
   
	void addSymbol(String sym, Double val)
	{
		symTab.put(sym, val);
	}
   
	void clearSymbols()
	{
		symTab.clear();
	}
   
	void printSymbols()
	{
		System.out.println("Symbols:");        
		Iterator i = symTab.keySet().iterator();
		while(i.hasNext()){
				String key = (String)i.next();
				System.out.println("    \"" + key + "\" : " + symTab.get(key));
		}
	}

	int cTest = 0;
	int cFail = 0;
	void test(String expr, boolean correctResult)
	{
		cTest++;
		printSymbols();
		System.out.println("Test #" + cTest + " \"" + expr + "\"");
		System.out.println("Should be: " + correctResult);
	   
		boolean actualResult;
		boolean failed = true;
		try
		{
			actualResult = evaluate (expr);
			failed = (actualResult != correctResult);
			System.out.println(failed ? "But it is not. Failure!" : "And it is. Success!");
		}
		catch (Exception e)
		{
			System.out.println("Syntax Error");
			e.printStackTrace();
		}              
		if (failed) cFail++;
		System.out.println();
	}

	void syntaxTest(String expr, boolean isSyntaxCorrect)
	{
		cTest++;
		printSymbols();
		System.out.println("Test #" + cTest + " \"" + expr + "\"");
		System.out.println("Syntax should be: " + (isSyntaxCorrect ? "correct" : "incorrect"));
	   
		boolean actualSyntaxCorrect = true;
		boolean failed = true;
		try
		{
			evaluate (expr);
			actualSyntaxCorrect = true;
		}
		catch (Exception e)
		{
			System.out.println("Syntax Error");
			e.printStackTrace();
			actualSyntaxCorrect = false;
		}   
		failed = (actualSyntaxCorrect != isSyntaxCorrect);
		if (failed) cFail++;
		System.out.println(failed ? "But it is not. Failure!" : "And it is. Success!");
		System.out.println();
	}
	
	void printResult()
	{
			System.out.println ();
			System.out.println ("=====================");
			System.out.println ("Result:");
			System.out.println ("Failed " + cFail + " out of " + cTest + " tests");
			System.out.println ("=====================");
	}
   
	// current input, used onlt in evaluate and getToken
	String input;
	
	char eatChar()
	{
		if (input.length() == 0)
		{
			return '\0';
		}
		else
		{
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
							throw new Exception();
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
							throw new Exception();
						}
						break;
				case '\0':
						token = new Token (Token.TOKEN_END);
						break;
				default:
						throw new Exception();
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
				throw new Exception();
			}			
		}
		else
		{
			nextToken = null;
			throw new Exception();
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
	
	boolean evaluate (String expr) throws Exception
	{
		System.out.println("Parse tree:");
		input = expr;
		
		Token e = expression();
		Token t = getToken();
		if (t.type != Token.TOKEN_END)
		{
			nextToken = null;
			throw new Exception();
		}
		e.printMe(1);
		return e.evaluateAsBool();
	}
   
	public void run()
	{
		clearSymbols();
		addSymbol ("x", 5.0);
		addSymbol ("y", -1.0);
	
		syntaxTest ("5 = 5 = 5", false);
		syntaxTest ("5 < 6 > 5", false);
		syntaxTest ("abcd", false);
		syntaxTest ("[x] < -0.5 3", false);
		syntaxTest ("[x] < -0.5 AND", false);
		syntaxTest ("([x] < -0.5", false);
		syntaxTest ("([x] < -0.5)", true);
		
		test ("[x] < -0.5", false);
		test ("5.0 > [y]", true);
		test ("[x] = 5.0", true);
		test ("[y] = -5.0", false);
		test ("[x] < 0 AND [y] < 0", false);
		test ("[x] < 0 AND [y] > 0", false);
		test ("[x] > 0 AND [y] < 0", true);
		test ("[x] > 0 AND [y] > 0", false);
		test ("[x] = 0 AND [y] = 0 OR [x] = 5.0 AND [y] = -1.0", true);
		test ("([x] = 0 AND [y] = 0) OR ([x] = 5.0 AND [y] = -1.0)", true);
		test ("[x] = 0 AND ([y] = 0 OR [x] = -5.0) AND [y] = -1.0", false);
		clearSymbols();
		addSymbol ("jouw waarde", 5.0);
		addSymbol ("mijn waarde", -1.0);
		test ("[jouw waarde] < 0 OR [mijn waarde] < 0", true);
		test ("[jouw waarde] < 0 OR [mijn waarde] > 0", false);
		test ("[jouw waarde] > 0 OR [mijn waarde] < 0", true);
		test ("[jouw waarde] > 0 OR [mijn waarde] > 0", true);
		printResult();
	}
   
	public static void main (String args[])
	{
		Main main = new Main();
		main.run();
	}
}
