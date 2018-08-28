/*
 * Copyright 2015 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.termd.core.readline;

/**
 * A function event.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class FunctionEvent extends KeyEventSupport {

  private final String name;
  private final int[] seq;

  public FunctionEvent(String name, int[] seq) {
    this.name = name;
    this.seq = seq;
  }

  /**
   * @return the name of the function to apply.
   */
  String name() {
    return name;
  }

  @Override
  public int getCodePointAt(int index) throws IndexOutOfBoundsException {
    if (index < 0 || index > seq.length) {
      throw new IndexOutOfBoundsException("Wrong index: " + index);
    }
    return seq[index];
  }

  @Override
  public int length() {
    return seq.length;
  }
}
