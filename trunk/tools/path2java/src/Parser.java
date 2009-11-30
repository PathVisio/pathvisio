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
import java.awt.geom.Point2D;

class Parser
{
	private Tokenizer tokenizer;
	private Emitter emitter;

	Parser (String path, Emitter _emitter) throws PathParseException
	{
		tokenizer = new Tokenizer (path);
		emitter = _emitter;
	}

	/**
	   only public method: parses a string and emits symbols to the emitter.
	*/
	public void parse () throws PathParseException
	{
		while (!tokenizer.lookAt().testFlag (Token.F_END))
		{
			eatExpression ();
		}
		emitter.flush();
	}

	/**
	   if the next token is whitespace, eat it
	   otherwise do nothing
	*/
	void eatWhitespace () throws PathParseException
	{
		if (tokenizer.lookAt().testFlag (Token.F_WHITESPACE))
		{
			tokenizer.eat();
		}
	}

	boolean eatFlag() throws PathParseException
	{
		eatWhitespace();
		Token t = tokenizer.eat();
		if (!t.testFlag(Token.F_FLAG)) { throw new PathParseException("Flag expected"); }
		eatWhitespace();
		return (t.getBoolean());
	}

	double eatNumber() throws PathParseException
	{
		eatWhitespace();
		Token t = tokenizer.eat();
		if (!t.testFlag(Token.F_NUMBER)) { throw new PathParseException("Number expected"); }
		eatWhitespace();
		return t.getDouble();
	}

	Point2D eatCoords() throws PathParseException
	{
		eatWhitespace();
		double x, y;
		if (tokenizer.lookAt().testFlag(Token.F_NUMBER))
		{
			x = tokenizer.eat().getDouble();
		}
		else
		{
			throw new PathParseException ("Number expected");
		}
		if (tokenizer.lookAt().testFlag(Token.F_WHITESPACE_OR_COMMA))
		{
			tokenizer.eat();
		}
		else
		{
			throw new PathParseException ("Whitespace and/or comma expected");
		}
		if (tokenizer.lookAt().testFlag(Token.F_NUMBER))
		{
			y = tokenizer.eat().getDouble();
		}
		else
		{
			throw new PathParseException ("Number expected");
		}
		eatWhitespace();
		return new Point2D.Double(x, y);
	}

	void eatExpression () throws PathParseException
	{
		eatWhitespace();
		Token t = tokenizer.eat();
		if (t.testFlag(Token.F_END)) { return; } // the end
		if (!t.testFlag(Token.F_COMMAND))
		{
			throw new PathParseException ("Command, whitespace or end expected");
		}
		char command = t.getCommand();
		if (command == 'z' || command == 'Z')
		{
			emitter.close();
			return;
		}
		do
		{
			switch (command)
			{
			case 'M':
				emitter.move (eatCoords());
				break;
			case 'm':
				emitter.moveRelative (eatCoords());
				break;
			case 'L':
				emitter.line (eatCoords());
				break;
			case 'l':
				emitter.lineRelative (eatCoords());
				break;
			case 'H':
				emitter.horizontal (eatNumber());
				break;
			case 'h':
				emitter.horizontalRelative (eatNumber());
				break;
			case 'V':
				emitter.vertical (eatNumber());
				break;
			case 'v':
				emitter.verticalRelative (eatNumber());
				break;
			case 'C':
				emitter.cubic (eatCoords(), eatCoords(), eatCoords());
				break;
			case 'c':
				emitter.cubicRelative (eatCoords(), eatCoords(), eatCoords());
				break;
			case 'S':
				emitter.smoothCube (eatCoords(), eatCoords());
				break;
			case 's':
				emitter.smoothCubeRelative (eatCoords(), eatCoords());
				break;
			case 'Q':
				emitter.quad (eatCoords(), eatCoords());
				break;
			case 'q':
				emitter.quadRelative (eatCoords(), eatCoords());
				break;
			case 'T':
				emitter.smoothQuad (eatCoords());
				break;
			case 't':
				emitter.smoothQuadRelative (eatCoords());
				break;
			case 'A':
				emitter.arc (eatCoords(), eatNumber(), eatFlag(), eatFlag(), eatCoords());
				break;
			case 'a':
				emitter.arcRelative (eatCoords(), eatNumber(), eatFlag(), eatFlag(), eatCoords());
				break;
			default:
				throw new PathParseException("Unknown command: '" + t.getCommand() + "'");
			}
		}
		while (tokenizer.lookAt().testFlag (Token.F_NUMBER));
	}
}
