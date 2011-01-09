/*
 *  KvJ Compiler for XML WZ data files
 *  Copyright (C) 2010, 2011  GoldenKevin
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kvjcompiler.mob;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import kvjcompiler.Converter;
import kvjcompiler.LittleEndianWriter;
import kvjcompiler.Size;
import kvjcompiler.mob.structure.Attack;
import kvjcompiler.mob.structure.SelfDestruct;
import kvjcompiler.mob.structure.Skill;

public class MobConverter extends Converter {
	private static final byte
		LEVEL = 1,
		MAX_HP = 2,
		MAX_MP = 3,
		PHYSICAL_DAMAGE = 4,
		EXP = 5,
		UNDEAD = 6,
		ELEM_ATTR = 7,
		REMOVE_AFTER = 8,
		HIDE_HP = 9,
		HIDE_NAME = 10,
		HP_TAG_COLOR = 11,
		HP_TAG_BG_COLOR = 12,
		BOSS = 13,
		SELF_DESTRUCT = 14,
		LOSE_ITEM = 15,
		INVINCIBLE = 16,
		REVIVE = 17,
		FIRST_ATTACK = 18,
		ATTACK = 19,
		SKILL = 20,
		BUFF = 21,
		DELAY = 22
	;
	
	private Map<String, Integer> delays;
	
	public void compile(String outPath, String internalPath, String imgName, XMLStreamReader r) throws XMLStreamException, IOException {
		this.delays = new HashMap<String, Integer>();
		startCompile(outPath, internalPath, imgName, r);
		
		String key;
		for (Entry<String, Integer> pair : delays.entrySet()) {
			key = pair.getKey();
			fos.write(new LittleEndianWriter(Size.HEADER + key.length() + 1 + Size.INT, DELAY).writeNullTerminatedString(key).writeInt(pair.getValue().intValue()).toArray());
		}
		
		finalizeCompile(internalPath, imgName);
	}
	
	public String getWzName() {
		return "Mob.wz";
	}
	
	protected void handleDir(String nestedPath) throws XMLStreamException, IOException {
		String[] dirs = nestedPath.split("/");
		if (dirs[0].equals("info")) {
			for (int open1 = 1, event, open; open1 > 0;) {
				event = r.next();
				if (event == XMLStreamReader.START_ELEMENT) {
					open1++;
					String key = r.getAttributeValue(0);
					if (key.equals("skill")) {
						Skill s;
						LittleEndianWriter lew;
						for (int open2 = 1; open2 > 0;) {
							event = r.next();
							if (event == XMLStreamReader.START_ELEMENT) {
								open2++;
								s = new Skill(Integer.parseInt(r.getAttributeValue(0)));
								for (open = 1; open > 0;) {
									event = r.next();
									if (event == XMLStreamReader.START_ELEMENT) {
										open++;
										s.setProperty(r.getAttributeValue(0), r.getAttributeValue(1));
									}
									if (event == XMLStreamReader.END_ELEMENT) {
										open--;
									}
								}
								lew = new LittleEndianWriter(Size.HEADER + s.size(), SKILL);
								s.writeBytes(lew);
								fos.write(lew.toArray());
							}
							if (event == XMLStreamReader.END_ELEMENT) {
								open2--;
							}
						}
					} else if (key.equals("selfDestruction")) {
						SelfDestruct sd;
						LittleEndianWriter lew;
						sd = new SelfDestruct();
						for (open = 1; open > 0;) {
							event = r.next();
							if (event == XMLStreamReader.START_ELEMENT) {
								open++;
								sd.setProperty(r.getAttributeValue(0), r.getAttributeValue(1));
							} else if (event == XMLStreamReader.END_ELEMENT) {
								open--;
							}
						}
						lew = new LittleEndianWriter(Size.HEADER + sd.size(), SELF_DESTRUCT);
						sd.writeBytes(lew);
						fos.write(lew.toArray());
					} else if (key.equals("loseItem")) {
						for (open = 1; open > 0;) {
							event = r.next();
							if (event == XMLStreamReader.START_ELEMENT) {
								open++;
								if (r.getAttributeValue(0).equals("id"))
									fos.write(new LittleEndianWriter(Size.HEADER + Size.INT, LOSE_ITEM).writeInt(Integer.parseInt(r.getAttributeValue(1))).toArray());
							} else if (event == XMLStreamReader.END_ELEMENT) {
								open--;
							}
						}
					} else if (key.equals("revive")) {
						for (open = 1; open > 0;) {
							event = r.next();
							if (event == XMLStreamReader.START_ELEMENT) {
								open++;
								fos.write(new LittleEndianWriter(Size.HEADER + Size.INT, REVIVE).writeInt(Integer.parseInt(r.getAttributeValue(1))).toArray());
							} else if (event == XMLStreamReader.END_ELEMENT) {
								open--;
							}
						}
					} else {
						String value = r.getAttributeValue(1);
						if (key.equals("level")) {
							fos.write(new LittleEndianWriter(Size.HEADER + Size.INT, LEVEL).writeInt(Integer.parseInt(value)).toArray());
						} else if (key.equals("maxHP")) {
							fos.write(new LittleEndianWriter(Size.HEADER + Size.INT, MAX_HP).writeInt(Integer.parseInt(value)).toArray());
						} else if (key.equals("maxMP")) {
							fos.write(new LittleEndianWriter(Size.HEADER + Size.INT, MAX_MP).writeInt(Integer.parseInt(value)).toArray());
						} else if (key.equals("PADamage")) {
							fos.write(new LittleEndianWriter(Size.HEADER + Size.INT, PHYSICAL_DAMAGE).writeInt(Integer.parseInt(value)).toArray());
						} else if (key.equals("exp")) {
							fos.write(new LittleEndianWriter(Size.HEADER + Size.INT, EXP).writeInt(Integer.parseInt(value)).toArray());
						} else if (key.equals("undead")) {
							if (Integer.parseInt(value) == 1)
								fos.write(new LittleEndianWriter(Size.HEADER, UNDEAD).toArray());
						} else if (key.equals("elemAttr")) {
							fos.write(new LittleEndianWriter(Size.HEADER + value.length() + 1, ELEM_ATTR).writeNullTerminatedString(value).toArray());
						} else if (key.equals("removeAfter")) {
							fos.write(new LittleEndianWriter(Size.HEADER + Size.INT, REMOVE_AFTER).writeInt(Integer.parseInt(value)).toArray());
						} else if (key.equals("hideHP")) {
							if (Integer.parseInt(value) == 1)
								fos.write(new LittleEndianWriter(Size.HEADER, HIDE_HP).toArray());
						} else if (key.equals("hideName")) {
							if (Integer.parseInt(value) == 1)
								fos.write(new LittleEndianWriter(Size.HEADER, HIDE_NAME).toArray());
						} else if (key.equals("hpTagColor")) {
							fos.write(new LittleEndianWriter(Size.HEADER + Size.INT, HP_TAG_COLOR).writeInt(Integer.parseInt(value)).toArray());
						} else if (key.equals("hpTagBgcolor")) {
							fos.write(new LittleEndianWriter(Size.HEADER + Size.INT, HP_TAG_BG_COLOR).writeInt(Integer.parseInt(value)).toArray());
						} else if (key.equals("boss")) {
							if (Integer.parseInt(value) == 1)
								fos.write(new LittleEndianWriter(Size.HEADER, BOSS).toArray());
						} else if (key.equals("invincible")) {
							if (Integer.parseInt(value) == 1)
								fos.write(new LittleEndianWriter(Size.HEADER, INVINCIBLE).toArray());
						} else if (key.equals("firstAttack")) {
							if (Float.parseFloat(value) > 0)
								fos.write(new LittleEndianWriter(Size.HEADER, FIRST_ATTACK).toArray());
						} else if (key.equals("buff")) {
							fos.write(new LittleEndianWriter(Size.HEADER + Size.INT, BUFF).writeInt(Integer.parseInt(value)).toArray());
						}
					}
				}
				if (event == XMLStreamReader.END_ELEMENT) {
					open1--;
				}
			}
			return;
		} else if (dirs[0].length() > 6 && dirs[0].substring(0, 6).equals("attack")) { //there should be a more elegant solution...
			Attack a = new Attack(Integer.parseInt(dirs[0].substring(6, dirs[0].length())));
			for (int open = 1, event; open > 0;) {
				event = r.next();
				if (event == XMLStreamReader.START_ELEMENT) {
					open++;
					if (open == 3) {
						if (r.getAttributeValue(0).equals("delay")) {
							if (!delays.containsKey(dirs[0]))
								delays.put(dirs[0], Integer.valueOf(0));
							delays.put(dirs[0], Integer.valueOf(delays.get(dirs[0]).intValue() + Integer.parseInt(r.getAttributeValue(1))));
						} else {
							a.setProperty(r.getAttributeValue(0), r.getAttributeValue(1));
						}
					}
				} else if (event == XMLStreamReader.END_ELEMENT) {
					open--;
				}
			}
			LittleEndianWriter lew = new LittleEndianWriter(Size.HEADER + a.size(), ATTACK);
			a.writeBytes(lew);
			fos.write(lew.toArray());
			return;
		} else {
			for (int open = 1, event; open > 0;) {
				event = r.next();
				if (event == XMLStreamReader.START_ELEMENT) {
					open++;
					if (r.getAttributeValue(0).equals("delay")) {
						if (!delays.containsKey(dirs[0]))
							delays.put(dirs[0], Integer.valueOf(0));
						delays.put(dirs[0], Integer.valueOf(delays.get(dirs[0]).intValue() + Integer.parseInt(r.getAttributeValue(1))));
					}
				} else if (event == XMLStreamReader.END_ELEMENT) {
					open--;
				}
			}
			return;
		}
		//traverseBlock(nestedPath);
	}
	
	protected void handleProperty(String nestedPath, String value) throws IOException {
		//System.out.println("DEBUG: Handling " + nestedPath);
		//String[] dirs = nestedPath.split("/");
	}
}