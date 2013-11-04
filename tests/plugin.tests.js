/*
 * JBoss, Home of Professional Open Source.
 * Copyright Red Hat, Inc., and individual contributors
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


describe('Geo fencing object', function () {
	it("geo plugin should exist", function() {
        expect(geofencing).toBeDefined();
		expect(typeof geofencing == 'object').toBe(true);
	});

    it("should contain a register function", function() {
        expect(geofencing.register).toBeDefined();
        expect(typeof geofencing.register == 'function').toBe(true);
    });
    
    it("should contain an addRegion function", function() {
        expect(geofencing.addRegion).toBeDefined();
        expect(typeof geofencing.addRegion == 'function').toBe(true);
    });
    
    it("should contain a removeRegion function", function() {
        expect(geofencing.removeRegion).toBeDefined();
        expect(typeof geofencing.removeRegion == 'function').toBe(true);
    });
    it("should contain a getWatchedRegionIds function", function() {
        expect(geofencing.getWatchedRegionIds).toBeDefined();
        expect(typeof geofencing.getWatchedRegionIds == 'function').toBe(true);
    });
});