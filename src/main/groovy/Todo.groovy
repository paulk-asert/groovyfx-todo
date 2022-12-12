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
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.TableCell
import javafx.scene.image.ImageView

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
}

@Canonical
@JsonIncludeProperties(['task', 'category', 'date'])
@FXBindable
class ToDoItem {
    String task
    ToDoCategory category
    LocalDate date
}

var file = 'todolist.json' as File
var mapper = new ObjectMapper().registerModule(new JavaTimeModule())
var open = { mapper.readValue(it, new TypeReference<List<ToDoItem>>() {}) }
var init = file.exists() ? open(file) : []
var items = FXCollections.observableList(init)
var close = { mapper.writeValue(file, items) }
var table, task, category, date, images = [:]
var urls = ToDoCategory.values().collectEntries {
    [it, "emoji/${Integer.toHexString(it.emoji.codePointAt(0))}.png"]
}

start {
    var style1 = [padding: 5, spacing: 5]
    var style2 = [minWidth: 80, alignment: RIGHT]
    stage(title: 'GroovyFX ToDo Demo', show: true, onCloseRequest: close) {
        urls.each { k, v -> images[k] = image(url: v, width: 24, height: 24) }
        scene {
            vbox(*:style1) {
                hbox(*:style1) {
                    label('Task:', *:style2)
                    task = textField()
                }
                hbox(*:style1) {
                    label('Category:', *:style2)
                    category = comboBox(items: ToDoCategory.values().toList(),
                    cellFactory: {
                        new ListCell<ToDoCategory>() {
                            void updateItem(ToDoCategory cat, boolean empty) {
                                super.updateItem(cat, empty)
                                if (empty) return
                                graphic = new Label(cat.name()).tap {
                                    it.graphic = new ImageView(images[cat])
                                }
                            }
                        }
                    })
                }
                hbox(*:style1) {
                    label('Date:', *:style2)
                    date = datePicker()
                }
                table = tableView(items: items) {
                    tableColumn(property: 'task', text: 'Task', prefWidth: 200)
                    tableColumn(property: 'category', text: 'Category', prefWidth: 80,
                            cellValueFactory: { new ReadOnlyObjectWrapper(it.value) },
                            cellFactory: {
                                new TableCell<ToDoItem, ToDoItem>() {
                                    void updateItem(ToDoItem item, boolean empty) {
                                        graphic = empty ? null : new ImageView(images[item.category])
                                        alignment = Pos.CENTER
                                    }
                                }
                            })
                    tableColumn(property: 'date', text: 'Date', prefWidth: 90,
                            type: Date, alignment: CENTER)
                }
                hbox(*:style1, alignment: CENTER) {
                    button('Add', onAction: {
                        if (task.text && category.value && date.value) {
                            items << new ToDoItem(task.text, category.value, date.value)
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
