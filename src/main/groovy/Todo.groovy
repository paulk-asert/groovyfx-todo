/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.fasterxml.jackson.annotation.JsonIncludeProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import groovy.transform.Canonical
import groovyx.javafx.beans.FXBindable
import javafx.collections.FXCollections

import java.time.LocalDate

import static groovyx.javafx.GroovyFX.start

enum ToDoCategory {
    EXERCISE("🚴"),
    WORK("📊"),
    RELAX("🧘"),
    TV("📺"),
    READ("📚"),
    EVENT("🎭"),
    CODE("💻"),
    COFFEE("☕️"),
    EAT("🍽"),
    SHOP("🛒"),
    SLEEP("😴")

    final String emoji

    ToDoCategory(String emoji) {
        this.emoji = emoji
    }

    String toString() { emoji }
}

@Canonical
@JsonIncludeProperties(['name', 'category', 'date'])
class ToDoItem {
    @FXBindable String name
    @FXBindable ToDoCategory category
    @FXBindable LocalDate date
}

var file = 'todolist.json' as File
var mapper = new ObjectMapper().registerModule(new JavaTimeModule())
var items = FXCollections.observableList(mapper.readValue(file, new TypeReference<List<ToDoItem>>() {}))
var table, item, category, date

start {
    stage(title: 'GroovyFX ToDo Demo', show: true, onCloseRequest: { event ->
        mapper.writeValue(file, items)
    }) {
        scene {
            vbox(padding: 10, spacing: 10) {
                hbox(spacing: 5) {
                    label('Item:', minWidth: 100, alignment: RIGHT)
                    item = textField()
                }
                hbox(spacing: 5) {
                    label('Category:', minWidth: 100, alignment: RIGHT)
                    category = choiceBox(items: ToDoCategory.values().toList())
                }
                hbox(spacing: 5) {
                    label('Date:', minWidth: 100, alignment: RIGHT)
                    date = datePicker()
                }
                table = tableView(items: items) {
                    tableColumn(property: 'name', text: 'Name', prefWidth: 200)
                    tableColumn(property: 'category', text: 'Category', prefWidth: 80)
                    tableColumn(property: 'date', text: 'Date', prefWidth: 100,
                            type: Date, converter: { from -> from.format('yyyy-MM-dd') }
                    )
                }
                hbox(spacing: 10, alignment: CENTER) {
                    button('Add', onAction: {
                        if (item.content.get() && category.value && date.value) {
                            items << new ToDoItem(item.content.get(), category.value, date.value)
                        }
                    })
                    button('Remove', onAction: {
                        items.removeAll(table.selectionModel.selectedItems)
                    })
                }
            }
        }
    }
}
