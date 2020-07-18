package celestial.beans.driver;

import java.util.Random;
import celestial.core.EngineRuntime;
import celestial.error.CelestialGenericException;
import celestial.util.Factory;

public class ExpressionDriver extends Driver {
	
	private static final long serialVersionUID = -2267140262916521747L;
	
	public static final Factory<ExpressionDriver> FACTORY = () -> new ExpressionDriver("");
	
	private String expression;
	private float time = 0;
	
	private final Random random = new Random();
	
	public ExpressionDriver(String expression) {
		this.expression = expression;
	}
	
	public String getExpression() {
		return expression;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	@Override
	protected void update() {
		float prevVal = super.value;
		super.value = new Object() {
			int ch, pos = -1;
			
			void nextChar() {
				ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
			}
			
			boolean consume(char ch) {
				while(this.ch == '\n' || this.ch == '\r' || this.ch == '\t' || this.ch == ' ') nextChar();
				if(this.ch == ch) {
					nextChar();
					return true;
				}
				return false;
			}
			
			float parse() {
				if(expression.length() == 0) return 0;
				nextChar();
				float value = parseExpression();
				if(ch != -1) throw new CelestialGenericException("Bad expression");
				return value;
			}
			
			float parseExpression() {
				float value = parseTerm();
				for(;;) {
					if(consume('+')) value += parseTerm();
					else if(consume('-')) value -= parseTerm();
					else return value;
				}
			}
			
			float parseTerm() {
				float value = parseFactor();
				for(;;) {
					if(consume('*')) value *= parseFactor();
					else if(consume('/')) value /= parseFactor();
					else if(consume('%')) value %= parseFactor();
					else return value;
				}
			}
			
			float parseFactor() {
				if(consume('+')) return parseFactor();
				if(consume('-')) return -parseFactor();
				
				int startPos = this.pos;
				float value;
				
				if(consume('(')) {
					value = parseExpression();
					consume(')');
				}
				else if((ch >= '0' && ch <= '9') || ch == '.') {
					while((ch >= '0' && ch <= '9') || ch == '.') nextChar();
					value = Float.parseFloat(expression.substring(startPos, this.pos));
				}
				else if((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '_') {
					while((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '_') nextChar();
					String func = expression.substring(startPos, this.pos);
					if(func.equals("abs")) value = Math.abs(parseFactor());
					else if(func.equals("sin")) value = (float) Math.sin(parseFactor());
					else if(func.equals("cos")) value = (float) Math.cos(parseFactor());
					else if(func.equals("tan")) value = (float) Math.tan(parseFactor());
					else if(func.equals("log")) value = (float) Math.log10(parseFactor());
					else if(func.equals("ln")) value = (float) Math.log(parseFactor());
					else if(func.equals("floor")) value = (float) ((int) parseFactor());
					else if(func.equals("rand")) value = random.nextFloat();
					else if(func.equals("time")) value = (time += EngineRuntime.frameTimeRelative());
					else if(func.equals("ans")) value = prevVal;
					else throw new CelestialGenericException("Bad expression");
				}
				else throw new CelestialGenericException("Bad expression");
				
				if(consume('!')) value = factorial(value);
				if(consume('^')) value = (float) Math.pow(value, parseFactor());
				
				return value;
			}
			
			float factorial(float value) {
				if(value <= 1) return value;
				else return value * factorial(value - 1f);
			}
			
		}.parse();
	}
	
	public boolean isValidExpression(String expr) {
		float tmpTime = time;
		float tmpValue = super.value;
		String tmpExpr = expression;
		expression = expr;
		
		try {
			update();
			
			time = tmpTime;
			super.value = tmpValue;
			expression = tmpExpr;
			return true;
		}
		catch(CelestialGenericException ex) {
			time = tmpTime;
			super.value = tmpValue;
			expression = tmpExpr;
			return false;
		}
	}
	
	@Override
	protected ExpressionDriver clone() {
		return new ExpressionDriver(expression);
	}
	
}
