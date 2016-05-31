/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.pnu.sync;


import android.Manifest;
import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import ru.pnu.common.accounts.GenericAccountService;
import ru.pnu.sync.provider.FeedContract;

/**
 * Фрагмент списка, содержащий список объектов входа Atom (статьи), которые хранятся в локальной базе данных.
 *
 * <Р> Доступ к базе данных обеспечивается с помощью контент-провайдера, указанный в
 * {@link ru.pnu.sync.provider.FeedProvider}. Это содержание
 * Провайдер
 * Заполняется автоматически через {@link SyncService}.
 *
 * <P> При выборе элемента из отображаемого списка отображает статью в браузере по умолчанию.
 *
 * <P> Если поставщик контента не возвращает никаких данных, то первая синхронизация еще не работать. Эта синхронизация
 * Адаптер принимает данные существуют в поставщике один раз в режим синхронизации запуска. Если ваше приложение не работает, как
 * Это, вы должны добавить флаг, который отмечает, если синхронизация закончилась, так что вы можете различать "нет
 * Имеющиеся данные "и" без начальной синхронизации ", и отображать это в пользовательском интерфейсе.
 *
 * <Р> ActionBar отображает кнопку "Обновить". Когда пользователь нажимает кнопку "Обновить", адаптер синхронизации
 * Запускается сразу. отображается неопределенными элемент ProgressBar, показывающий, что синхронизация
 * Происходит.
 */
public class EntryListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "EntryListFragment";

    /**
     * Курсор адаптер для управления ListView результатов.
     */
    private SimpleCursorAdapter mAdapter;

    /**
     * Обращайтесь к SyncObserver. Элемент ProgressBar виден, пока не сообщает SyncObserver
     * Что синхронизация завершена.
     *
     * <Р> Это позволяет удалить нашу SyncObserver когда-то приложение больше не в
     * На первом плане.
     */
    private Object mSyncObserverHandle;

    /**
     * Меню опций используется для заполнения ActionBar.
     */
    private Menu mOptionsMenu;

    /**
     * Проекция для выполнения запросов к поставщику содержимого.
     */
    private static final String[] PROJECTION = new String[]{
            FeedContract.Entry._ID,
            FeedContract.Entry.COLUMN_NAME_TITLE,
            FeedContract.Entry.COLUMN_NAME_LINK,
            FeedContract.Entry.COLUMN_NAME_PUBLISHED
    };

    // Column indexes. The index of a column in the Cursor is the same as its relative position in
    // the projection.
    /** Column index for _ID */
    private static final int COLUMN_ID = 0;
    /** Column index for title */
    private static final int COLUMN_TITLE = 1;
    /** Column index for link */
    private static final int COLUMN_URL_STRING = 2;
    /** Column index for published */
    private static final int COLUMN_PUBLISHED = 3;

    /**
     * Список столбцов курсора для чтения при подготовке адаптера для заполнения ListView.
     */
    private static final String[] FROM_COLUMNS = new String[]{
            FeedContract.Entry.COLUMN_NAME_TITLE,
            FeedContract.Entry.COLUMN_NAME_PUBLISHED
    };

    /**
     * List of Views which will be populated by Cursor data.
     */
    private static final int[] TO_FIELDS = new int[]{
            android.R.id.text1,
            android.R.id.text2};
    /**
     * Permissions required to read and write contacts.
     */
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS};
    /**
     * Id to identify a contacts permission request.
     */
    private static final int REQUEST_CONTACTS = 1;

    /**
     * Обязательный пустой конструктор для менеджера фрагмента для конкретизации
     * Фрагмент (например, при изменении ориентации экрана).
     */
    public EntryListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Contacts permissions have not been granted.
                Log.i(TAG, "Contact permissions has NOT been granted. Requesting permissions.");
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.READ_CONTACTS)
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.WRITE_CONTACTS)) {

                    // Provide an additional rationale to the user if the permission was not granted
                    // and the user would benefit from additional context for the use of the permission.
                    // For example, if the request has been denied previously.
                    Log.i(TAG,
                            "Displaying contacts permission rationale to provide additional context.");

                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_CONTACT, REQUEST_CONTACTS);
                    Toast.makeText(getContext(),
                            getContext().getText(R.string.start),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // Contact permissions have not been granted yet. Request them directly.
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_CONTACT, REQUEST_CONTACTS);
                    Toast.makeText(getContext(),
                            getContext().getText(R.string.permission),
                            Toast.LENGTH_SHORT)
                            .show();
                    Log.i(TAG, "Lol");
                }
            } else {

                // Contact permissions have been granted. Show the contacts fragment.
                Log.i(TAG,
                        "Contact permissions have already been granted. Displaying contact details.");
//                SyncUtils.TriggerRefresh(getContext());
            }
        } else {
//            SyncUtils.TriggerRefresh(getContext());
        }
        setHasOptionsMenu(true);
    }

    /**
     * Создание SyncAccount на старте, если это необходимо.
     *
     * <Р> Это создаст новую учетную запись с системой для нашего приложения, зарегистрировать нашу
     * {@link SyncService} с ним, а также установить расписание синхронизации.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Create account, if needed
        SyncUtils.CreateSyncAccount(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new SimpleCursorAdapter(
                getActivity(),       // Current context
                android.R.layout.simple_list_item_activated_2,  // Layout for individual rows
                null,                // Cursor
                FROM_COLUMNS,        // Cursor columns to use
                TO_FIELDS,           // Layout fields to use
                0                    // No flags
        );
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if (i == COLUMN_PUBLISHED) {
                    // Convert timestamp to human-readable date
                    Time t = new Time();
                    t.set(cursor.getLong(i));
                    ((TextView) view).setText(t.format("%Y-%m-%d %H:%M"));
                    return true;
                } else {
                    // Let SimpleCursorAdapter handle other fields automatically
                    return false;
                }
            }
        });
//        setListAdapter(mAdapter);
//        setEmptyText(getText(R.string.loading));
//        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    /**
     * Запрос поставщика контента для данных.
     *
     * <Р> Погрузчики делать запросы в фоновом потоке. Они также обеспечивают ContentObserver, который
     * Срабатывает при изменении данных в контент-провайдера. Когда адаптер синхронизации обновляет
     * Контент-провайдер, то ContentObserver реагирует путем сброса загрузчик, а затем загрузить
     * Это.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // We only have one loader, so we can ignore the value of i.
        // (It'll be '0', as set in onCreate().)
        return new CursorLoader(getActivity(),  // Context
                FeedContract.Entry.CONTENT_URI, // URI
                PROJECTION,                // Projection
                null,                           // Selection
                null,                           // Selection args
                FeedContract.Entry.COLUMN_NAME_PUBLISHED + " desc"); // Sort
    }

    /**
     * Переместить курсор, возвращаемый запросом к адаптеру ListView. Это обновляет существующий
     * UI с данными курсора.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    /**
     * Вызывается, когда ContentObserver определено для провайдера контента обнаруживает, что данные
     * Изменен. ContentObserver сбрасывает загрузчик, а затем повторно запускает загрузчик. В адаптера,
     * Установите значение курсора на NULL. Это удаляет ссылку на курсор, что позволяет ему быть
     * Сборщиком мусора.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.changeCursor(null);
    }

    /**
     * Create the ActionBar.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mOptionsMenu = menu;
        inflater.inflate(R.menu.main, menu);
    }

    /**
     * Ответить на пользовательские жесты на панели действий.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:

                if (Build.VERSION.SDK_INT >= 23) {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS)
                            != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CONTACTS)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Contacts permissions have not been granted.
                        Log.i(TAG, "Contact permissions has NOT been granted. Requesting permissions.");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                Manifest.permission.READ_CONTACTS)
                                || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                Manifest.permission.WRITE_CONTACTS)) {

                            // Provide an additional rationale to the user if the permission was not granted
                            // and the user would benefit from additional context for the use of the permission.
                            // For example, if the request has been denied previously.
                            Log.i(TAG,
                                    "Displaying contacts permission rationale to provide additional context.");

                            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_CONTACT, REQUEST_CONTACTS);

                        } else {
                            // Contact permissions have not been granted yet. Request them directly.
                            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_CONTACT, REQUEST_CONTACTS);
                            Toast.makeText(getContext(),
                                    getContext().getText(R.string.permission),
                                    Toast.LENGTH_SHORT)
                                    .show();
                            Log.i(TAG, "Lol");
                        }

                    } else {

                        // Contact permissions have been granted. Show the contacts fragment.
                        Log.i(TAG,
                                "Contact permissions have already been granted. Displaying contact details.");
                        Toast.makeText(getContext(),
                                getContext().getText(R.string.start),
                                Toast.LENGTH_SHORT)
                                .show();
                        SyncUtils.TriggerRefresh(getContext());
                    }
                } else {
                    Toast.makeText(getContext(),
                            getContext().getText(R.string.start),
                            Toast.LENGTH_SHORT)
                            .show();
                    SyncUtils.TriggerRefresh(getContext());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void showContacts(View v) {
        Log.i(TAG, "Show contacts button pressed. Checking permissions.");

        // Verify that all required contact permissions have been granted.

    }

    private void requestContactsPermissions() {
        // BEGIN_INCLUDE(contacts_permission_request)

        // END_INCLUDE(contacts_permission_request)
    }

    /**
     * Загрузить статью в браузере по умолчанию при выборе пользователем.
     */
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Get a URI for the selected item, then start an Activity that displays the URI. Any
        // Activity that filters for ACTION_VIEW and a URI can accept this. In most cases, this will
        // be a browser.

        // Get the item at the selected position, in the form of a Cursor.
        Cursor c = (Cursor) mAdapter.getItem(position);
        // Get the link to the article represented by the item.
        String articleUrlString = c.getString(COLUMN_URL_STRING);
        if (articleUrlString == null) {
            Log.e(TAG, "Attempt to launch entry with null link");
            return;
        }

        Log.i(TAG, "Opening URL: " + articleUrlString);
        // Get a Uri object for the URL string
        Uri articleURL = Uri.parse(articleUrlString);
        Intent i = new Intent(Intent.ACTION_VIEW, articleURL);
        startActivity(i);
    }

    /**
     * Установите состояние кнопки Обновить. Если синхронизация активна, включите виджет Progress Bar.
     * В противном случае, выключите его.
     *
     * @param refreshing True если активная синхронизация происходит, иначе false
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setRefreshActionButtonState(boolean refreshing) {
        if (mOptionsMenu == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    /**
     * Crfate новый анонимный SyncStatusObserver. Он прикреплен к ContentResolver о приложении в
     * OnResume (), и удаляется в OnPause (). Если изменения статуса, он устанавливает состояние Обновить
     * Кнопка. Если синхронизация активна или в ожидании, кнопка Refresh заменяется неопределенная
     * Индикатор; в противном случае, отображается сама кнопка.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            getActivity().runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = GenericAccountService.GetAccount(SyncUtils.ACCOUNT_TYPE);
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        setRefreshActionButtonState(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, FeedContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, FeedContract.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };

}