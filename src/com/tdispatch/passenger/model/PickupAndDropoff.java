package com.tdispatch.passenger.model;


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
	protected LocationData mPickup;
	protected LocationData mDropoff;

	public PickupAndDropoff() {
		// dummy
	}

	public PickupAndDropoff(LocationData pickup, LocationData dropoff) {
		mPickup = pickup;
		mDropoff = dropoff;
	}

	public PickupAndDropoff setPickup( LocationData data ) {
		mPickup = data;
		return this;

	}

	public LocationData getPickup() {
		return mPickup;
	}

	public PickupAndDropoff setDropoff( LocationData data ) {
		mDropoff = data;
		return this;
	}

	public LocationData getDropoff() {
		return mDropoff;
	}

}
