package com.tdispatch.passenger.model;

import com.google.android.gms.maps.model.LatLng;

/*
 ******************************************************************************
 *
 * Copyright (C) 2013 T Dispatch Ltd
 *
 * Licensed under the GPL License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 *
 * @author Marcin Orlowski <marcin.orlowski@webnet.pl>
 *
 ******************************************************************************
 */
public class PickupAndDropoff
{
	protected LatLng mPickup;
	protected LatLng mDropoff;

	public PickupAndDropoff() {
		// dummy
	}

	public PickupAndDropoff(LatLng pickup, LatLng dropoff) {
		mPickup = pickup;
		mDropoff = dropoff;
	}

	public PickupAndDropoff setPickup( LatLng data ) {
		mPickup = data;
		return this;

	}

	public LatLng getPickup() {
		return mPickup;
	}

	public PickupAndDropoff setDropoff( LatLng data ) {
		mDropoff = data;
		return this;
	}

	public LatLng getDropoff() {
		return mDropoff;
	}

}
