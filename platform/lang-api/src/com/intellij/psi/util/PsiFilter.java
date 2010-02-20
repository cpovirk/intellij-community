/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.psi.util;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class PsiFilter<T extends PsiElement> {
  private final Class<T> filter;
  public static final PsiFilter<?>[] EMPTY = {}; 

  public PsiFilter(@NotNull Class<T> filter) {
    this.filter = filter;
  }

  public boolean accept(T element) {
    return true;
  }

  public final Class<T> getParentClass() {
    return filter; 
  }

  public boolean areEquivalent(T e1, T e2) {
    return e1.isEquivalentTo(e2);
  }
}
