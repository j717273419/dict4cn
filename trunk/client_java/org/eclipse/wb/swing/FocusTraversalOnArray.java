/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

/**
 * Cyclic focus traversal policy based on array of components.
 * <p>
 * This class may be freely distributed as part of any application or plugin.
 * 
 * @author scheglov_ke
 */
public class FocusTraversalOnArray extends FocusTraversalPolicy {
  private final Component m_Components[];

  // //////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // //////////////////////////////////////////////////////////////////////////
  public FocusTraversalOnArray(final Component components[]) {
    this.m_Components = components;
  }

  // //////////////////////////////////////////////////////////////////////////
  //
  // Utilities
  //
  // //////////////////////////////////////////////////////////////////////////
  private int indexCycle(final int index, final int delta) {
    final int size = this.m_Components.length;
    final int next = (index + delta + size) % size;
    return next;
  }

  private Component cycle(final Component currentComponent, final int delta) {
    int index = -1;
    loop: for (int i = 0; i < this.m_Components.length; i++) {
      final Component component = this.m_Components[i];
      for (Component c = currentComponent; c != null; c = c.getParent()) {
        if (component == c) {
          index = i;
          break loop;
        }
      }
    }
    // try to find enabled component in "delta" direction
    final int initialIndex = index;
    while (true) {
      final int newIndex = this.indexCycle(index, delta);
      if (newIndex == initialIndex) {
        break;
      }
      index = newIndex;
      //
      final Component component = this.m_Components[newIndex];
      if (component.isEnabled() && component.isVisible() && component.isFocusable()) {
        return component;
      }
    }
    // not found
    return currentComponent;
  }

  // //////////////////////////////////////////////////////////////////////////
  //
  // FocusTraversalPolicy
  //
  // //////////////////////////////////////////////////////////////////////////
  @Override
  public Component getComponentAfter(final Container container, final Component component) {
    return this.cycle(component, 1);
  }

  @Override
  public Component getComponentBefore(final Container container, final Component component) {
    return this.cycle(component, -1);
  }

  @Override
  public Component getFirstComponent(final Container container) {
    return this.m_Components[0];
  }

  @Override
  public Component getLastComponent(final Container container) {
    return this.m_Components[this.m_Components.length - 1];
  }

  @Override
  public Component getDefaultComponent(final Container container) {
    return this.getFirstComponent(container);
  }
}
