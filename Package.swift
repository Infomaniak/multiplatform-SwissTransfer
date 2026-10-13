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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-0.12.0-202610130832-38369042068-1/STCore.xcframework.zip",
            checksum: "ffbe68a946ac70db58fbebc788896cde478cb5f366e4d17ccc04459ef1bccc01"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-0.12.0-202610130832-38369042068-1/STDatabase.xcframework.zip",
            checksum: "312beed0241a24cb363d6c3d042999129b9b698d60fe608f005c6ed5f1fedc8f"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-0.12.0-202610130832-38369042068-1/STNetwork.xcframework.zip",
            checksum: "56383bd14b5804629da5320a5af4c0694b2c12c81207480512a5c83c24de6ef3"
        ),
    ]
)
