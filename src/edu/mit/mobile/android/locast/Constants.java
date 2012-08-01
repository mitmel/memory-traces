package edu.mit.mobile.android.locast;

/*
 * Copyright (C) 2011  MIT Mobile Experience Lab
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

public class Constants {

	public static final String AUTHORITY = "edu.mit.mobile.android.locast";

	/**
	 * General debugging flag. This needs to be a constant so that the compiler will remove any
	 * debugging code.
	 */
	public static final boolean DEBUG = false;

	/**
	 * If true, this uses the Android account framework for storing accounts. Make sure the
	 * appropriate permissions are uncommented in the AndroidManifest.xml. If this is false, all
	 * requests will be anonymous and content creation will be disabled.
	 */
	public static final boolean USE_ACCOUNT_FRAMEWORK = false;

	/**
	 * If true, the user can create casts.
	 */
	public static final boolean CAN_CREATE_CASTS = false;

	/**
	 * If false, enables a demo account that can be used to make non-authenticated requests.
	 */
	public static final boolean REQUIRE_LOGIN = false;

	/**
	 * Enables a built-in app update checker if the app is not going to be published on the Market.
	 */
	public static final boolean USE_APPUPDATE_CHECKER = true;

	/**
	 * For various cursor views, this says how frequently it should refresh the UI.
	 */
	public static final long UPDATE_THROTTLE = 500;

	/**
	 * If Google Maps is used, set to false. This enables various workarounds and API differences.
	 */
	public static final boolean USES_OSMDROID = false;
}
