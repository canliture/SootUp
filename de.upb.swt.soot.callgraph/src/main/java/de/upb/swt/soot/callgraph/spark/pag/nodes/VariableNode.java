package de.upb.swt.soot.callgraph.spark.pag.nodes;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.types.Type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a simple variable node (Green) in the pointer assignment graph.
 * */
public class VariableNode extends Node {

  // TODO: [kk] need a more precise type, or generics
  protected Object variable;
  private Map<Field, FieldReferenceNode> fields;


  public VariableNode(Object variable, Type type) {
    this.variable = variable;
    this.type = type;
  }

  public void addField(Field field, FieldReferenceNode fieldNode) {
    if (fields == null) {
      fields = new HashMap<>();
    }
    fields.put(field, fieldNode);
  }

  public Object getVariable() {
    return variable;
  }

  public FieldReferenceNode getField(Field field){
    return fields.get(field);
  }
}