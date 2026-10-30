// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.1-202610301457-41074521429-1/STCore.xcframework.zip",
            checksum: "c2632b313d2fbd1bac7c6d21b060e422229a17522c1949259c404287c0eb56ed"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.1-202610301457-41074521429-1/STDatabase.xcframework.zip",
            checksum: "fb8c36f713295473c4409816fa5410eae9895679ba38fe738bcb07321646e795"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.1-202610301457-41074521429-1/STNetwork.xcframework.zip",
            checksum: "3c730f44589147f5d5aa358b7a48b978eb9922529a43cd5d23c20535c646102d"
        ),
    ]
)
