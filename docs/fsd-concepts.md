# Full Stack Development evaluation map

| Concept | Location | What to demonstrate |
|---|---|---|
| Semantic HTML | All HTML pages; UI-generated nav/sections/tables | Header, aside, main, section, form, labels, table headings and footer structure. |
| CSS3 | `frontend/css/style.css` | Custom properties, finance visual hierarchy, cards, focus states and theme tokens. |
| Flexbox | Topbar, page actions, transaction rows, dialog actions | One-dimensional alignment and spacing. |
| CSS Grid | Metrics, charts, card grids, auth and profile layouts | Adaptive two-dimensional layout. |
| Responsive design | `responsive.css` | 1440/1024/768/480/360 layouts; sidebar drawer; scrollable tables. |
| Bootstrap 5 | Pinned CSS in HTML | Form controls, table utilities, buttons, layout utilities, spinner, progress and accessible helper classes. |
| JavaScript ES6+ | ES modules in `frontend/js` | Imports/exports, const/let, template strings, array transforms and destructuring. |
| DOM manipulation | `ui.js` and page renderers | HTML from API data, event handlers, text updates and reusable modal creation. |
| Events | Form submit, click delegation, change, Escape, themechange | A browser event triggers validation, an HTTP request and a data-driven re-render. |
| Forms | Auth, transaction, budget, goal, profile and filters | Labeled fields, required inputs, number/date constraints and contextual messages. |
| Validation | Native constraints plus page validation; backend DTOs | Client validation improves usability; backend validation remains authoritative. |
| Fetch API | `api.js` | Headers, JSON encoding, bearer authorization, non-2xx errors, blob downloads. |
| Async/await | Every page module | Readable asynchronous flows; `Promise.all` for independent dashboard calls. |
| JSON | Request DTOs and response records | Show a transaction object in DevTools Network and its matching Java DTO. |
| REST | Spring controllers | GET reads, POST creates, PUT replaces editable fields, DELETE removes resources. |
| CRUD | Transactions, categories, budgets, goals | Each visible action calls an actual REST route and persists in MySQL. |
| Authentication | `auth.js`, `api.js`, JWT security filter | Login obtains token; protected requests send it; logout revokes it server-side. |
| Authorization | Scoped backend queries | A second account cannot get/edit/delete the first account's records. |
| Search/filter/sort | `transactions.js`, query DTO, JPA Specification | Actual SQL filtering with paginated results, not client-only array filtering. |
| Chart.js | `charts.js`, dashboard, budgets, reports | Bar, doughnut, line and budget comparison charts fed by server aggregates. |
| Modals | `formModal` and `confirmDelete` in `ui.js` | Native `dialog` with labels, focus, Escape/cancel and real submit/delete actions. |
| Alerts/toasts | `toast`, loadSection, inline notices | Save confirmation, recoverable errors and budget warnings. |
| State handling | Session token, page query state, theme preference | Browser storage is not the database; refresh retrieves persisted server data. |
| CSV downloads | Central blob download helper and Java export service | Actual server-produced UTF-8 file; all matching rows, not only visible page. |
| Accessibility | Labels, skip links, visible focus, ARIA, native dialogs | Keyboard navigation, offscreen navigation made inert, button labels, reduced motion. |
| Security-aware rendering | `escape`/`textContent` | A saved `<script>`-like description appears as text, not executable markup. |
| Database integration | Fetch → controllers → services → JPA → MySQL | Change one record and verify transaction history, dashboard and budget all agree. |

## Network panel demonstration

Open DevTools → Network → Fetch/XHR. Add an expense and inspect:

1. POST request URL and JSON body.
2. Authorization header (do not display real tokens in a published recording).
3. 201 response with the persisted ID and category name.
4. Follow-up GET of the paginated list.
5. Dashboard aggregate updates and the corresponding row in MySQL.

## Responsive/accessibility demonstration

Use device emulation at 1440, 1024, 768, 480 and 360 pixels. Cards collapse from four to two columns; goal/budget cards eventually become one column. Tables scroll inside their container. Mobile navigation uses a labeled toggle, backdrop and Escape closing; closed mobile links are inert. Dialog fields have unique IDs even when a filter and modal share a field name. Theme changes update both CSS and Chart.js colors.

Canvas charts carry meaningful accessible names; exact totals are also shown in cards/tables/reports. This is basic accessibility support, not a claim of formal accessibility certification.
