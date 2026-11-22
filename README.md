## Educational CMS

A desktop Content Management System for articles, blogs, and media files, built with JavaFX + FXML following the MVC pattern, backed by an embedded SQLite database, with PDF report generation (iText 7). 
Designed as a teaching example for a Java course / educational event.

## Features
- User accounts & login
- BCrypt-hashed passwords, self-service registration, three roles: ADMIN, EDITOR, AUTHOR.
- Dashboard — live counts, a status pie chart, a content-type bar chart, and a recent activity feed.
- Articles & Blog Posts — full CRUD, search/filter by title, category, tag, or status. Authors can only edit their own drafts; Editors/Admins can manage everything.
- Media Library — upload files (copied into media_store/), track filename/type/size/uploader, open or delete from the library.
- User Management (Admin only) — change roles, remove accounts.
- PDF Reports — one click generates a full system report (content summary, media inventory, users, recent activity) via iText 7.
- Role-based navigation — sidebar items and edit/delete controls adapt to the signed-in user's role.
- Model — plain data classes in model/, persisted through dao/.
- View — declarative .fxml layouts in resources/.../view/, styled by style.css. No business logic lives in FXML.
- Controller — one controller class per view, wired via fx:controller. Controllers call into service//dao/ layers, never raw SQL directly in the UI layer.
- Database - Ships with SQLite (zero setup — a file edu_cms.db is created next to the app on first run) so it's easy to run in a classroom. The schema and queries are simple enough to retarget at MySQL:

*username: admin*

*password: Admin@123*

## Running the project
Requires JDK 17+ and Maven. Run from the project root:

mvn clean javafx:run
Or build a runnable fat jar:

mvn clean package
java -jar target/edu-cms.jar
Uploaded media files are copied into a media_store/ folder created next to the application; PDF reports are saved wherever you choose in the save-file dialog reachable from the sidebar's Reports button (visible to Editors and Admins).
