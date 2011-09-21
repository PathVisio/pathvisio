// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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
import junit.framework.TestCase;

class Test extends TestCase
{
	void testTokenizer()
	{
		Token t = null;
		try { t = new Tokenizer ("1").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_NOT_DOUBLE | Token.F_FLAG);
		assertEquals (t.getDouble(), 1.0, 0.001);
		assertEquals (t.getBoolean(), true);
		try { t = new Tokenizer ("0").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_NOT_DOUBLE | Token.F_FLAG);
		assertEquals (t.getBoolean(), false);
		try { t = new Tokenizer ("-1").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_NOT_DOUBLE);
		assertEquals (t.getDouble(), -1, 0.001);
		try { t = new Tokenizer (".1").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_DOUBLE);
		assertEquals (t.getDouble(), 0.1, 0.001);
		try { t = new Tokenizer ("1.").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_DOUBLE);
		assertEquals (t.getDouble(), 1.0, 0.001);
		try { t = new Tokenizer ("1e2").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_DOUBLE);
		assertEquals (t.getDouble(), 100.0, 0.001);
		try { t = new Tokenizer (".1e2").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_DOUBLE);
		assertEquals (t.getDouble(), 10.0, 0.001);
		try { t = new Tokenizer ("1.e2").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_DOUBLE);
		assertEquals (t.getDouble(), 100.0, 0.001);
		try { t = new Tokenizer ("1e-2").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_DOUBLE);
		assertEquals (t.getDouble(), 0.01, 0.001);
		try { t = new Tokenizer ("-.5").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_DOUBLE);
		assertEquals (t.getDouble(), -0.5, 0.001);
		try { t = new Tokenizer ("-5.").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_DOUBLE);
		assertEquals (t.getDouble(), -5, 0.001);
		try { t = new Tokenizer ("-.5e-2").lookAt(); } catch (PathParseException e) { fail ("Unexpected exception"); }
		assertEquals (t.getFlags(), Token.F_NUMBER | Token.F_DOUBLE);
		assertEquals (t.getDouble(), -0.5e-2, 0.001);
	}
}
