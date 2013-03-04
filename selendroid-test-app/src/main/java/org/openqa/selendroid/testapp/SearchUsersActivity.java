package org.openqa.selendroid.testapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SearchUsersActivity extends Activity {

  private ListView mListView;
  List<String> results = new ArrayList<String>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.search);

    mListView = (ListView) findViewById(R.id.searchlistview);
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
