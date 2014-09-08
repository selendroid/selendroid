/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.selendroid.testapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import io.selendroid.testapp.*;

public class SearchUsersActivity extends Activity {

  private ListView mListView;
  List<String> results = new ArrayList<String>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(io.selendroid.testapp.R.layout.search);

    mListView = (ListView) findViewById(io.selendroid.testapp.R.id.searchlistview);
    ArrayAdapter<String> words =
        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1,
            results);
    mListView.setAdapter(words);
    Intent intent = getIntent();

    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      // handles a search query
      String query = intent.getStringExtra(SearchManager.QUERY);
      showResults(query);
    }
  }

  @Override
  public boolean onSearchRequested() {
    Intent intent = getIntent();
    String query = intent.getStringExtra(SearchManager.QUERY);
    showResults(query);
    return super.onSearchRequested();
  }

  /**
   * Searches the dictionary and displays results for the given query.
   * 
   * @param query The search query
   */
  private void showResults(String query) {
    if ("cars".equals(query)) {
      results.clear();
      results.add("Volkswagen");
      results.add("Mercedes Benz");
      results.add("BMW");
      results.add("Porsche");
      results.add("Tesla");
    } else if ("phones".equals(query)) {
      results.add("Nexus 4");
      results.add("Galaxy Nexus");
      results.add("Galaxy 7");
    }
  }
}
