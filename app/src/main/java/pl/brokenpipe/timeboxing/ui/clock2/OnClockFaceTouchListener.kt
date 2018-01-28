/*
 * *
 *  * Copyright 2017 Grzegorz Wierzchanowski
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package pl.brokenpipe.timeboxing.ui.clock2

/**
 * Created by wierzchanowskig on 29.07.2017.
 */
interface OnClockFaceTouchListener {
    interface HandleDragStart {
        fun onHandleDragStart(d: Double)
    }

    interface HandleDragStop {
        fun onHandleDragStop(d: Double)
    }

    interface HandleDragging {
        fun onHandleDragging(angle: Double)
    }
}