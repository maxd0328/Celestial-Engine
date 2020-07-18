package celestial.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.lwjgl.input.Mouse;

/**
 * Work in progress, use native input methods for now
 */
public final class SystemInput {
	
	private static final Object LOCK = new Object();
	
	private static final LinkedHashMap<Integer, Boolean> BUTTON_MAP = new LinkedHashMap<Integer, Boolean>();
	private static final LinkedHashMap<Integer, Boolean> BUTTON_MAP_QUEUE = new LinkedHashMap<Integer, Boolean>();
	
	public static boolean isButtonDown(int button) {
		return Mouse.isButtonDown(button);
	}
	
	public static boolean isButtonPressed(int button) {
		synchronized(LOCK) {
			return BUTTON_MAP.get(button);
		}
	}
	
	protected static void init() {
		synchronized(LOCK) {
			for(int i = 0 ; i < 6 ; ++i) {
				BUTTON_MAP.put(i, false);
				BUTTON_MAP_QUEUE.put(i, true);
			}
		}
	}
	
	protected static void update() {
		synchronized(LOCK) {
			for(int button : BUTTON_MAP.keySet()) {
				if(Mouse.isButtonDown(button) && BUTTON_MAP_QUEUE.get(button)) {
					BUTTON_MAP.put(button, true);
					BUTTON_MAP_QUEUE.put(button, false);
				}
				else {
					BUTTON_MAP.put(button, false);
					if(!Mouse.isButtonDown(button)) BUTTON_MAP_QUEUE.put(button, true);
				}
			}
		}
	}
	
	public static Map<String, Integer> getKeyMap() {
		HashMap<String, Integer> keyMap = new HashMap<String, Integer>();
		
		keyMap.put("KEY_NONE", 0x00);
		
		keyMap.put("KEY_ESCAPE", 0x01);
		keyMap.put("KEY_1", 0x02);
		keyMap.put("KEY_2", 0x03);
		keyMap.put("KEY_3", 0x04);
		keyMap.put("KEY_4", 0x05);
		keyMap.put("KEY_5", 0x06);
		keyMap.put("KEY_6", 0x07);
		keyMap.put("KEY_7", 0x08);
		keyMap.put("KEY_8", 0x09);
		keyMap.put("KEY_9", 0x0A);
		keyMap.put("KEY_0", 0x0B);
		keyMap.put("KEY_MINUS", 0x0C); /* - on main keyboard */
		keyMap.put("KEY_EQUALS", 0x0D);
		keyMap.put("KEY_BACK", 0x0E); /* backspace */
		keyMap.put("KEY_TAB", 0x0F);
		keyMap.put("KEY_Q", 0x10);
		keyMap.put("KEY_W", 0x11);
		keyMap.put("KEY_E", 0x12);
		keyMap.put("KEY_R", 0x13);
		keyMap.put("KEY_T", 0x14);
		keyMap.put("KEY_Y", 0x15);
		keyMap.put("KEY_U", 0x16);
		keyMap.put("KEY_I", 0x17);
		keyMap.put("KEY_O", 0x18);
		keyMap.put("KEY_P", 0x19);
		keyMap.put("KEY_LBRACKET", 0x1A);
		keyMap.put("KEY_RBRACKET", 0x1B);
		keyMap.put("KEY_RETURN", 0x1C); /* Enter on main keyboard */
		keyMap.put("KEY_LCONTROL", 0x1D);
		keyMap.put("KEY_A", 0x1E);
		keyMap.put("KEY_S", 0x1F);
		keyMap.put("KEY_D", 0x20);
		keyMap.put("KEY_F", 0x21);
		keyMap.put("KEY_G", 0x22);
		keyMap.put("KEY_H", 0x23);
		keyMap.put("KEY_J", 0x24);
		keyMap.put("KEY_K", 0x25);
		keyMap.put("KEY_L", 0x26);
		keyMap.put("KEY_SEMICOLON", 0x27);
		keyMap.put("KEY_APOSTROPHE", 0x28);
		keyMap.put("KEY_GRAVE", 0x29); /* accent grave */
		keyMap.put("KEY_LSHIFT", 0x2A);
		keyMap.put("KEY_BACKSLASH", 0x2B);
		keyMap.put("KEY_Z", 0x2C);
		keyMap.put("KEY_X", 0x2D);
		keyMap.put("KEY_C", 0x2E);
		keyMap.put("KEY_V", 0x2F);
		keyMap.put("KEY_B", 0x30);
		keyMap.put("KEY_N", 0x31);
		keyMap.put("KEY_M", 0x32);
		keyMap.put("KEY_COMMA", 0x33);
		keyMap.put("KEY_PERIOD", 0x34); /* . on main keyboard */
		keyMap.put("KEY_SLASH", 0x35); /* / on main keyboard */
		keyMap.put("KEY_RSHIFT", 0x36);
		keyMap.put("KEY_MULTIPLY", 0x37); /* * on numeric keypad */
		keyMap.put("KEY_LMENU", 0x38); /* left Alt */
		keyMap.put("KEY_SPACE", 0x39);
		keyMap.put("KEY_CAPITAL", 0x3A);
		keyMap.put("KEY_F1", 0x3B);
		keyMap.put("KEY_F2", 0x3C);
		keyMap.put("KEY_F3", 0x3D);
		keyMap.put("KEY_F4", 0x3E);
		keyMap.put("KEY_F5", 0x3F);
		keyMap.put("KEY_F6", 0x40);
		keyMap.put("KEY_F7", 0x41);
		keyMap.put("KEY_F8", 0x42);
		keyMap.put("KEY_F9", 0x43);
		keyMap.put("KEY_F10", 0x44);
		keyMap.put("KEY_NUMLOCK", 0x45);
		keyMap.put("KEY_SCROLL", 0x46); /* Scroll Lock */
		keyMap.put("KEY_NUMPAD7", 0x47);
		keyMap.put("KEY_NUMPAD8", 0x48);
		keyMap.put("KEY_NUMPAD9", 0x49);
		keyMap.put("KEY_SUBTRACT", 0x4A); /* - on numeric keypad */
		keyMap.put("KEY_NUMPAD4", 0x4B);
		keyMap.put("KEY_NUMPAD5", 0x4C);
		keyMap.put("KEY_NUMPAD6", 0x4D);
		keyMap.put("KEY_ADD", 0x4E); /* + on numeric keypad */
		keyMap.put("KEY_NUMPAD1", 0x4F);
		keyMap.put("KEY_NUMPAD2", 0x50);
		keyMap.put("KEY_NUMPAD3", 0x51);
		keyMap.put("KEY_NUMPAD0", 0x52);
		keyMap.put("KEY_DECIMAL", 0x53); /* . on numeric keypad */
		keyMap.put("KEY_F11", 0x57);
		keyMap.put("KEY_F12", 0x58);
		keyMap.put("KEY_F13", 0x64); /*                     (NEC PC98) */
		keyMap.put("KEY_F14", 0x65); /*                     (NEC PC98) */
		keyMap.put("KEY_F15", 0x66); /*                     (NEC PC98) */
		keyMap.put("KEY_F16", 0x67); /* Extended Function keys - (Mac) */
		keyMap.put("KEY_F17", 0x68);
		keyMap.put("KEY_F18", 0x69);
		keyMap.put("KEY_KANA", 0x70); /* (Japanese keyboard)            */
		keyMap.put("KEY_F19", 0x71); /* Extended Function keys - (Mac) */
		keyMap.put("KEY_CONVERT", 0x79); /* (Japanese keyboard)            */
		keyMap.put("KEY_NOCONVERT", 0x7B); /* (Japanese keyboard)            */
		keyMap.put("KEY_YEN", 0x7D); /* (Japanese keyboard)            */
		keyMap.put("KEY_NUMPADEQUALS", 0x8D); /* = on numeric keypad (NEC PC98) */
		keyMap.put("KEY_CIRCUMFLEX", 0x90); /* (Japanese keyboard)            */
		keyMap.put("KEY_AT", 0x91); /*                     (NEC PC98) */
		keyMap.put("KEY_COLON", 0x92); /*                     (NEC PC98) */
		keyMap.put("KEY_UNDERLINE", 0x93); /*                     (NEC PC98) */
		keyMap.put("KEY_KANJI", 0x94); /* (Japanese keyboard)            */
		keyMap.put("KEY_STOP", 0x95); /*                     (NEC PC98) */
		keyMap.put("KEY_AX", 0x96); /*                     (Japan AX) */
		keyMap.put("KEY_UNLABELED", 0x97); /*                        (J3100) */
		keyMap.put("KEY_NUMPADENTER", 0x9C); /* Enter on numeric keypad */
		keyMap.put("KEY_RCONTROL", 0x9D);
		keyMap.put("KEY_SECTION", 0xA7); /* Section symbol (Mac) */
		keyMap.put("KEY_NUMPADCOMMA", 0xB3); /* , on numeric keypad (NEC PC98) */
		keyMap.put("KEY_DIVIDE", 0xB5); /* / on numeric keypad */
		keyMap.put("KEY_SYSRQ", 0xB7);
		keyMap.put("KEY_RMENU", 0xB8); /* right Alt */
		keyMap.put("KEY_FUNCTION", 0xC4); /* Function (Mac) */
		keyMap.put("KEY_PAUSE", 0xC5); /* Pause */
		keyMap.put("KEY_HOME", 0xC7); /* Home on arrow keypad */
		keyMap.put("KEY_UP", 0xC8); /* UpArrow on arrow keypad */
		keyMap.put("KEY_PRIOR", 0xC9); /* PgUp on arrow keypad */
		keyMap.put("KEY_LEFT", 0xCB); /* LeftArrow on arrow keypad */
		keyMap.put("KEY_RIGHT", 0xCD); /* RightArrow on arrow keypad */
		keyMap.put("KEY_END", 0xCF); /* End on arrow keypad */
		keyMap.put("KEY_DOWN", 0xD0); /* DownArrow on arrow keypad */
		keyMap.put("KEY_NEXT", 0xD1); /* PgDn on arrow keypad */
		keyMap.put("KEY_INSERT", 0xD2); /* Insert on arrow keypad */
		keyMap.put("KEY_DELETE", 0xD3); /* Delete on arrow keypad */
		keyMap.put("KEY_CLEAR", 0xDA); /* Clear key (Mac) */
		keyMap.put("KEY_LMETA", 0xDB); /* Left Windows/Option key */
		
		return keyMap;
	}
	
}
