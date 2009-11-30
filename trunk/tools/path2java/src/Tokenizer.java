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
import java.util.regex.*;

class Tokenizer
{
	private String original;
	private String s;
	private String newS; // new string after token is eaten
	Token nextToken;

	public Tokenizer (String _s) throws PathParseException
	{
		s = _s;
		original = _s;
		nextToken = tokenize();
	}

	/**
	   returns the next token and moves ahead in the string
	*/
	public Token eat () throws PathParseException
	{
		Token result = nextToken;
		s = newS;
		nextToken = tokenize();
		return result;
	}

	/**
	   Returns the next token without moving ahead in the string
	*/
	public Token lookAt ()
	{
		return nextToken;
	}

	/**
	   returns the upcoming token, while setting newS to the string after the token.
	*/
	private Token tokenize () throws PathParseException
	{
		String VALID_COMMANDS = "MmZzLlHhVvCcSsQqTtAa";

		Pattern comma_wsp = Pattern.compile ("\\s*,\\s*");
		Pattern wsp = Pattern.compile ("\\s+");
		Pattern float_with_exponent = Pattern.compile ("[+-]?(\\.\\d+|\\d+\\.?\\d*)([eE][+-]?\\d+)");
		Pattern float_without_exponent = Pattern.compile ("[+-]?(\\d+\\.\\d*|\\d*\\.\\d+)");
		Pattern int_constant = Pattern.compile ("[+-]?\\d+");
		Pattern command = Pattern.compile ("[" + VALID_COMMANDS + "]");

		if (s == null || s.equals (""))
		{
			return new Token (Token.F_END);
		}

		Matcher m = comma_wsp.matcher (s);
		if (m.lookingAt())
		{
			newS = s.substring (m.end());
			return (new Token (Token.F_WHITESPACE_OR_COMMA | Token.F_COMMA));
		}
		m.usePattern (wsp);
		if (m.lookingAt())
		{
			newS = s.substring (m.end());
			return (new Token (Token.F_WHITESPACE_OR_COMMA | Token.F_WHITESPACE));
		}
		m.usePattern (float_without_exponent);
		if (m.lookingAt())
		{
			newS = s.substring (m.end());
			return new Token (
				Token.F_NUMBER | Token.F_DOUBLE,
				Double.parseDouble(m.group())
				);
		}

		m.usePattern (float_with_exponent);
		if (m.lookingAt())
		{
			newS = s.substring (m.end());
			return new Token (
				Token.F_NUMBER | Token.F_DOUBLE,
				Double.parseDouble(m.group())
				);
		}

		m.usePattern (int_constant);
		if (m.lookingAt())
		{
			newS = s.substring (m.end());
			int value = Integer.parseInt (m.group());
			return new Token (
				Token.F_NUMBER | Token.F_NOT_DOUBLE | ((value == 0 || value == 1) ? Token.F_FLAG : 0),
				value
				);
		}

		m.usePattern(command);
		if (m.lookingAt())
		{
			newS = s.substring (m.end());
			return new Token (m.group().charAt(0));
		}

		throw new PathParseException ("Parse error: unknown Token");
	}

}
