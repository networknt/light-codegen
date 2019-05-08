import React, {Component} from 'react';
import {withStyles} from "@material-ui/core";

const styles = theme => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    pre: {
        'white-space': 'pre-wrap',
    },
});

class Document extends Component {
    render() {
        const { classes } = this.props;
        return (
            <div>
                <pre className={classes.pre}>
                    Light-codegen is used to scaffold a new project with a specification file and a config file.
                </pre>
                <pre className={classes.pre}>
                    For document, please visit <a href="https://doc.networknt.com/tool/light-codegen/" rel="noreferrer noopener" target="_blank">reference</a>.
                </pre>
                <pre className={classes.pre}>
                    To learn how to use each generator, please visit <a href="https://doc.networknt.com/tutorial/generator/" rel="noreferrer noopener" target="_blank">tutorial</a>.
                </pre>
            </div>
        )
    }
}

export default withStyles(styles)(Document);