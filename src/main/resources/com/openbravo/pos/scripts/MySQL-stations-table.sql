--    uniCenta oPOS is free software: you can redistribute it and/or modify
--    it under the terms of the GNU General Public License as published by
--    the Free Software Foundation, either version 3 of the License, or
--    (at your option) any later version.
--
--    uniCenta oPOS is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU General Public License for more details.
--
--    You should have received a copy of the GNU General Public License
--    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

/*
 * Script created by Jack, uniCenta 20/11/2016 08:00:00
 *
 * Create STATIONS table for Send To Station
*/

/* Header line. Object: orders. Script date: 01/01/2017 00:00:01. */
CREATE TABLE IF NOT EXISTS `stations` (
    `id` varchar(255) NOT NULL,
    `name` varchar(255) NOT NULL,
    `printer` varchar(255) DEFAULT NULL,
    `ip` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT = Compact;
 
